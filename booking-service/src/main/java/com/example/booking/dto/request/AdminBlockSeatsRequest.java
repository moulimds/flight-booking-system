package com.example.booking.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminBlockSeatsRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotEmpty(message = "At least one seat number is required")
    private List<String> seatNumbers;

    private String reason;
}
