package com.flight.seat.dto;

import java.time.LocalDateTime;

public class SeatLockResponse {

    private boolean locked;
    private String lockKey;
    private Long flightId;
    private String seatNumber;
    private Long userId;
    private long remainingTtlSeconds;
    private LocalDateTime lockedAt;
    private LocalDateTime expiresAt;
    private String message;

    public SeatLockResponse() {}

    public SeatLockResponse(boolean locked, String lockKey, Long flightId, String seatNumber, Long userId,
                            long remainingTtlSeconds, LocalDateTime lockedAt, LocalDateTime expiresAt, String message) {
        this.locked = locked;
        this.lockKey = lockKey;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.remainingTtlSeconds = remainingTtlSeconds;
        this.lockedAt = lockedAt;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getLockKey() { return lockKey; }
    public void setLockKey(String lockKey) { this.lockKey = lockKey; }

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public long getRemainingTtlSeconds() { return remainingTtlSeconds; }
    public void setRemainingTtlSeconds(long remainingTtlSeconds) { this.remainingTtlSeconds = remainingTtlSeconds; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}