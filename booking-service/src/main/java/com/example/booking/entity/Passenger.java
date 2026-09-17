package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 20)
    private PassengerType passengerType;

    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "seat_number", length = 10)
    private String seatNumber;

    @Column(name = "individual_base_price", precision = 10, scale = 2)
    private java.math.BigDecimal individualBasePrice;

    @Column(name = "individual_tax_amount", precision = 10, scale = 2)
    private java.math.BigDecimal individualTaxAmount;

    @Column(name = "individual_total_amount", precision = 10, scale = 2)
    private java.math.BigDecimal individualTotalAmount;
}
