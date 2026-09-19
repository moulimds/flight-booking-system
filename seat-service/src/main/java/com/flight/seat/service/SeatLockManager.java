package com.flight.seat.service;

import com.flight.seat.dto.SeatLockRequest;
import com.flight.seat.dto.SeatLockResponse;
import com.flight.seat.entity.Seat;
import com.flight.seat.exception.InvalidSeatException;
import com.flight.seat.exception.SeatAlreadyBookedException;
import com.flight.seat.messaging.SeatEventPublisher;
import com.flight.seat.messaging.SeatHeldEvent;
import com.flight.seat.messaging.SeatReleasedEvent;
import com.flight.seat.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeatLockManager {

    private static final Logger logger = LoggerFactory.getLogger(SeatLockManager.class);

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatEventPublisher seatEventPublisher;

    public static class LockEntry {
        public final String lockKey;
        public final Long flightId;
        public final String seatNumber;
        public final Long userId;
        public final long lockedAtMillis;
        public final long expiresAtMillis;
        public final int ttlSeconds;

        public LockEntry(String lockKey, Long flightId, String seatNumber, Long userId, int ttlSeconds) {
            this.lockKey = lockKey;
            this.flightId = flightId;
            this.seatNumber = seatNumber;
            this.userId = userId;
            this.ttlSeconds = ttlSeconds;
            this.lockedAtMillis = System.currentTimeMillis();
            this.expiresAtMillis = this.lockedAtMillis + (ttlSeconds * 1000L);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtMillis;
        }

        public long remainingTtlSeconds() {
            long remaining = (expiresAtMillis - System.currentTimeMillis()) / 1000L;
            return Math.max(0, remaining);
        }
    }

    private final Map<String, LockEntry> activeLocks = new ConcurrentHashMap<>();

    public static String buildLockKey(Long flightId, String seatNumber) {
        return "LOCK:FLIGHT_" + flightId + ":SEAT_" + (seatNumber != null ? seatNumber.toUpperCase() : "");
    }

    @Transactional
    public synchronized SeatLockResponse acquireLock(SeatLockRequest request) {
        Long flightId = request.getFlightId();
        String seatNum = request.getSeatNumber().toUpperCase();
        Long userId = request.getUserId();
        int ttl = (request.getTtlSeconds() != null && request.getTtlSeconds() > 0) ? request.getTtlSeconds() : 600;
        String lockKey = buildLockKey(flightId, seatNum);

        LockEntry currentLock = activeLocks.get(lockKey);
        if (currentLock != null && !currentLock.isExpired()) {
            if (!currentLock.userId.equals(userId)) {
                logger.warn("Seat collision! {} is currently locked by userId: {}, remaining: {}s",
                        lockKey, currentLock.userId, currentLock.remainingTtlSeconds());
                throw new InvalidSeatException("Seat " + seatNum + " is currently locked by another passenger. Time remaining: "
                        + currentLock.remainingTtlSeconds() + "s");
            } else {
                logger.info("Refreshing lock for same user {} on {}", userId, lockKey);
            }
        }

        Optional<Seat> optSeat = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(flightId, seatNum);
        Seat seat;
        if (optSeat.isPresent()) {
            seat = optSeat.get();
            if ("BOOKED".equalsIgnoreCase(seat.getStatus())) {
                throw new SeatAlreadyBookedException("Seat " + seatNum + " on flight " + flightId + " is already booked.");
            }
            seat.setStatus("HELD");
            seat = seatRepository.saveAndFlush(seat);
        } else {
            seat = new Seat();
            seat.setFlightId(flightId);
            seat.setSeatNumber(seatNum);
            seat.setSeatClass("ECONOMY");
            seat.setStatus("HELD");
            seat = seatRepository.saveAndFlush(seat);
        }

        LockEntry newLock = new LockEntry(lockKey, flightId, seatNum, userId, ttl);
        activeLocks.put(lockKey, newLock);
        logger.info("Successfully acquired Redis-TTL lock: {} for userId: {} (TTL: {}s)",
                lockKey, userId, ttl);

        try {
            seatEventPublisher.publishSeatHeld(new SeatHeldEvent(seat.getId(), seat.getFlightId(), seat.getSeatNumber(), userId));
        } catch (Exception e) {
            logger.warn("Failed to publish SeatHeldEvent: {}", e.getMessage());
        }

        LocalDateTime lockedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(newLock.lockedAtMillis), ZoneId.systemDefault());
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(newLock.expiresAtMillis), ZoneId.systemDefault());

        return new SeatLockResponse(
                true,
                lockKey,
                flightId,
                seatNum,
                userId,
                newLock.remainingTtlSeconds(),
                lockedAt,
                expiresAt,
                "Seat successfully locked for " + (ttl / 60) + " minutes (TTL: " + ttl + "s)"
        );
    }

    @Transactional
    public synchronized boolean releaseLock(Long flightId, String seatNumber, Long userId) {
        String lockKey = buildLockKey(flightId, seatNumber);
        LockEntry entry = activeLocks.get(lockKey);
        if (entry != null) {
            if (userId == null || entry.userId.equals(userId)) {
                activeLocks.remove(lockKey);
                logger.info("Released lock for key: {}", lockKey);

                seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(flightId, seatNumber.toUpperCase()).ifPresent(seat -> {
                    if ("HELD".equalsIgnoreCase(seat.getStatus())) {
                        seat.setStatus("AVAILABLE");
                        seatRepository.saveAndFlush(seat);
                        try {
                            seatEventPublisher.publishSeatReleased(new SeatReleasedEvent(seat.getId(), seat.getFlightId(), seat.getSeatNumber()));
                        } catch (Exception e) {
                            logger.warn("Failed to publish SeatReleasedEvent: {}", e.getMessage());
                        }
                    }
                });
                return true;
            } else {
                logger.warn("User {} cannot release lock {} owned by user {}", userId, lockKey, entry.userId);
                return false;
            }
        }
        return false;
    }

    public synchronized void onSeatBooked(Long flightId, String seatNumber) {
        String lockKey = buildLockKey(flightId, seatNumber);
        activeLocks.remove(lockKey);
        logger.info("Cleared lock {} because seat is now confirmed/booked", lockKey);
    }

    public SeatLockResponse getLockStatus(Long flightId, String seatNumber) {
        String lockKey = buildLockKey(flightId, seatNumber);
        LockEntry entry = activeLocks.get(lockKey);
        if (entry != null && !entry.isExpired()) {
            LocalDateTime lockedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.lockedAtMillis), ZoneId.systemDefault());
            LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.expiresAtMillis), ZoneId.systemDefault());
            return new SeatLockResponse(true, lockKey, flightId, seatNumber.toUpperCase(), entry.userId,
                    entry.remainingTtlSeconds(), lockedAt, expiresAt, "Seat is currently LOCKED");
        }

        Optional<Seat> optSeat = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(flightId, seatNumber.toUpperCase());
        String status = optSeat.map(Seat::getStatus).orElse("AVAILABLE");
        if ("HELD".equalsIgnoreCase(status)) {
            optSeat.ifPresent(s -> {
                s.setStatus("AVAILABLE");
                seatRepository.saveAndFlush(s);
            });
            status = "AVAILABLE";
        }
        return new SeatLockResponse(false, lockKey, flightId, seatNumber.toUpperCase(), null,
                0, null, null, "Seat is not locked (status: " + status + ")");
    }

    @Scheduled(fixedRate = 2000)
    @Transactional
    public synchronized void runAutoReleaseEngine() {
        if (activeLocks.isEmpty()) {
            return;
        }

        for (Map.Entry<String, LockEntry> entry : activeLocks.entrySet()) {
            LockEntry lock = entry.getValue();
            if (lock.isExpired()) {
                String key = entry.getKey();
                logger.info("Auto-Release Engine: Lock {} expired after TTL. Evicting lock...", key);
                activeLocks.remove(key);

                seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(lock.flightId, lock.seatNumber).ifPresent(seat -> {
                    if ("HELD".equalsIgnoreCase(seat.getStatus())) {
                        seat.setStatus("AVAILABLE");
                        seatRepository.saveAndFlush(seat);
                        logger.info("Auto-Release Engine: Reverted seat {} on flight {} from HELD to AVAILABLE",
                                lock.seatNumber, lock.flightId);
                        try {
                            seatEventPublisher.publishSeatReleased(new SeatReleasedEvent(seat.getId(), seat.getFlightId(), seat.getSeatNumber()));
                        } catch (Exception e) {
                            logger.warn("Failed to publish SeatReleasedEvent during auto-release: {}", e.getMessage());
                        }
                    }
                });
            }
        }
    }
}