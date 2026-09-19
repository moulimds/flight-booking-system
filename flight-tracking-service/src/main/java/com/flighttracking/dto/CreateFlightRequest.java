package com.flighttracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlightRequest {

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotEmpty(message = "Flight must have at least one leg")
    @Valid
    private List<CreateFlightLegRequest> legs;
}
