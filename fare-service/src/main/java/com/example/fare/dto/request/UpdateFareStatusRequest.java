package com.example.fare.dto.request;

import com.example.fare.entity.FareStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFareStatusRequest {

    @NotNull(message = "Fare status is required")
    private FareStatus status;
}
