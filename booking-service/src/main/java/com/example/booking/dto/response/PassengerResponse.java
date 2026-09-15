package com.example.booking.dto.response;

import com.example.booking.entity.Gender;
import com.example.booking.entity.PassengerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private PassengerType passengerType;
    private String passportNumber;
    private String nationality;
    private String seatNumber;
    private java.math.BigDecimal individualBasePrice;
    private java.math.BigDecimal individualTaxAmount;
    private java.math.BigDecimal individualTotalAmount;
}
