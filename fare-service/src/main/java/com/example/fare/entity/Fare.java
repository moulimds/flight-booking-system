package com.example.fare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(name = "fare_code", nullable = false, length = 50)
    private String fareCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "fare_type", nullable = false, length = 20)
    private FareType fareType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 30)
    private CabinClass cabinClass;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FareStatus status;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fare_rule_id")
    private FareRule fareRule;

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
        if (this.status == null) {
            this.status = FareStatus.ACTIVE;
        }
        recalculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalculateTotalPrice();
    }

    public void recalculateTotalPrice() {
        if (this.basePrice != null && this.taxAmount != null) {
            this.totalPrice = this.basePrice.add(this.taxAmount);
        }
    }
}
