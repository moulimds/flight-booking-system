package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_pnr", columnList = "pnr", unique = true),
        @Index(name = "idx_booking_ref", columnList = "booking_reference", unique = true),
        @Index(name = "idx_booking_user", columnList = "user_id"),
        @Index(name = "idx_booking_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 30)
    private String bookingReference;

    @Column(name = "pnr", nullable = false, unique = true, length = 10)
    private String pnr;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "fare_id", nullable = false)
    private Long fareId;

    // Fare Price Snapshot
    @Column(name = "fare_code", length = 50)
    private String fareCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "fare_type", length = 20)
    private FareType fareType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", length = 30)
    private CabinClass cabinClass;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    // Statuses
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 30)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    private BigDecimal cancellationFee;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;

    @Column(name = "adult_count")
    @Builder.Default
    private Integer adultCount = 0;

    @Column(name = "child_count")
    @Builder.Default
    private Integer childCount = 0;

    @Column(name = "infant_count")
    @Builder.Default
    private Integer infantCount = 0;

    @Column(name = "adult_total", precision = 10, scale = 2)
    private BigDecimal adultTotal;

    @Column(name = "child_total", precision = 10, scale = 2)
    private BigDecimal childTotal;

    @Column(name = "infant_total", precision = 10, scale = 2)
    private BigDecimal infantTotal;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.currency == null) {
            this.currency = "INR";
        }
        if (this.bookingStatus == null) {
            this.bookingStatus = BookingStatus.INITIATED;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }
}
