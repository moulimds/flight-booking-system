package com.airline.seat.config;

import com.airline.seat.entity.Seat;
import com.airline.seat.entity.SeatClass;
import com.airline.seat.entity.SeatInventory;
import com.airline.seat.entity.SeatStatus;
import com.airline.seat.repository.SeatInventoryRepository;
import com.airline.seat.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SeatRepository seatRepository;
    private final SeatInventoryRepository inventoryRepository;

    public DataInitializer(SeatRepository seatRepository, SeatInventoryRepository inventoryRepository) {
        this.seatRepository = seatRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (seatRepository.count() > 0) {
            log.info("Seat database already initialized.");
            return;
        }

        log.info("Initializing flight seat maps and inventory seed data...");

        List<Seat> flight101Seats = new ArrayList<>();
        // Flight FL-101: First Class (Rows 1-2, Seats A, D)
        for (int r = 1; r <= 2; r++) {
            flight101Seats.add(new Seat("FL-101", r + "A", SeatClass.FIRST, true, new BigDecimal("2.50")));
            flight101Seats.add(new Seat("FL-101", r + "D", SeatClass.FIRST, true, new BigDecimal("2.50")));
        }

        // Business Class (Rows 3-5, Seats A, B, C, D)
        for (int r = 3; r <= 5; r++) {
            flight101Seats.add(new Seat("FL-101", r + "A", SeatClass.BUSINESS, true, new BigDecimal("1.80")));
            flight101Seats.add(new Seat("FL-101", r + "B", SeatClass.BUSINESS, false, new BigDecimal("1.60")));
            flight101Seats.add(new Seat("FL-101", r + "C", SeatClass.BUSINESS, false, new BigDecimal("1.60")));
            flight101Seats.add(new Seat("FL-101", r + "D", SeatClass.BUSINESS, true, new BigDecimal("1.80")));
        }

        // Economy Class (Rows 10-15, Seats A, B, C, D, E, F)
        for (int r = 10; r <= 15; r++) {
            boolean exitRow = (r == 12);
            flight101Seats.add(new Seat("FL-101", r + "A", SeatClass.ECONOMY, exitRow, exitRow ? new BigDecimal("1.25") : new BigDecimal("1.00")));
            flight101Seats.add(new Seat("FL-101", r + "B", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
            flight101Seats.add(new Seat("FL-101", r + "C", SeatClass.ECONOMY, exitRow, exitRow ? new BigDecimal("1.15") : new BigDecimal("1.00")));
            flight101Seats.add(new Seat("FL-101", r + "D", SeatClass.ECONOMY, exitRow, exitRow ? new BigDecimal("1.15") : new BigDecimal("1.00")));
            flight101Seats.add(new Seat("FL-101", r + "E", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
            flight101Seats.add(new Seat("FL-101", r + "F", SeatClass.ECONOMY, exitRow, exitRow ? new BigDecimal("1.25") : new BigDecimal("1.00")));
        }

        // Flight FL-202 (Airbus A320 layout)
        List<Seat> flight202Seats = new ArrayList<>();
        for (int r = 1; r <= 2; r++) {
            flight202Seats.add(new Seat("FL-202", r + "A", SeatClass.BUSINESS, true, new BigDecimal("1.75")));
            flight202Seats.add(new Seat("FL-202", r + "B", SeatClass.BUSINESS, true, new BigDecimal("1.75")));
        }
        for (int r = 5; r <= 10; r++) {
            flight202Seats.add(new Seat("FL-202", r + "A", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
            flight202Seats.add(new Seat("FL-202", r + "B", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
            flight202Seats.add(new Seat("FL-202", r + "C", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
            flight202Seats.add(new Seat("FL-202", r + "D", SeatClass.ECONOMY, false, new BigDecimal("1.00")));
        }

        List<Seat> saved101 = seatRepository.saveAll(flight101Seats);
        seatRepository.saveAll(flight202Seats);

        // Pre-populate Schedule SCH-101 inventories
        List<SeatInventory> inventories = new ArrayList<>();
        for (Seat seat : saved101) {
            SeatInventory inv = new SeatInventory(seat, "SCH-101", SeatStatus.AVAILABLE);

            // Set some demo states for testing
            if ("1A".equals(seat.getSeatNumber())) {
                inv.setStatus(SeatStatus.RESERVED);
                inv.setBookingReference("BK-9901");
            } else if ("3A".equals(seat.getSeatNumber())) {
                inv.setStatus(SeatStatus.HELD);
                inv.setHeldByUserId("USER-1001");
                inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
            } else if ("12A".equals(seat.getSeatNumber())) {
                inv.setStatus(SeatStatus.BLOCKED);
                inv.setBlockedReason("Emergency exit door inspection");
                inv.setBlockedBy("OPS-TEAM");
            }

            inventories.add(inv);
        }

        inventoryRepository.saveAll(inventories);
        log.info("Initialized {} seats and {} inventory records successfully.",
                flight101Seats.size() + flight202Seats.size(), inventories.size());
    }
}
