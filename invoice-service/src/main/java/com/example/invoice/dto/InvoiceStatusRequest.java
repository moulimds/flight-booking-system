package com.example.invoice.dto;

import com.example.invoice.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record InvoiceStatusRequest(

        @NotNull
        InvoiceStatus status

) {
}