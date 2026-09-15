package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_seats", indexes = {
        @Index(name = "idx_flight_seats_flight", columnList = "flight_id"),
        @Index(name = "idx_flight_seats_booking", columnList = "booking_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_flight_seat", columnNames = {"flight_id", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private CabinClass cabinClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status", nullable = false, length = 20)
    private SeatStatus seatStatus;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;

    @Version
    @Column(name = "version")
    private Long version;

    public boolean isCurrentlyAvailable() {
        if (this.seatStatus == SeatStatus.AVAILABLE) {
            return true;
        }
        if (this.seatStatus == SeatStatus.HELD && this.holdExpiresAt != null && this.holdExpiresAt.isBefore(LocalDateTime.now())) {
            return true;
        }
        return false;
    }
}
