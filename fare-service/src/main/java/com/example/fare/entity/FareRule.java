package com.example.fare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fare_id", nullable = true)
    private Long fareId;

    @Column(name = "baggage_allowance", length = 100)
    private String baggageAllowance;

    @Column(name = "cancellation_allowed", nullable = false)
    private Boolean cancellationAllowed;

    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    private BigDecimal cancellationFee;

    @Column(name = "date_change_allowed", nullable = false)
    private Boolean dateChangeAllowed;

    @Column(name = "date_change_fee", precision = 10, scale = 2)
    private BigDecimal dateChangeFee;

    @Column(name = "meal_selection_allowed", nullable = false)
    private Boolean mealSelectionAllowed;

    @Column(name = "seat_selection_allowed", nullable = false)
    private Boolean seatSelectionAllowed;

    @Column(name = "refund_allowed", nullable = false)
    private Boolean refundAllowed;
}
