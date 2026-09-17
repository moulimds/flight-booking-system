package com.example.payment.controller;


import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.payment.dto.RefundRequest;
import com.example.payment.dto.RefundResponse;
import com.example.payment.service.RefundService;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<RefundResponse> initiateRefund(
            @Valid @RequestBody RefundRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(refundService.initiateRefund(request));
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefund(
            @PathVariable String refundId) {

        return ResponseEntity.ok(
                refundService.getRefund(refundId)
        );
    }

    @PostMapping("/{refundId}/process")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable String refundId) {

        return ResponseEntity.ok(
                refundService.processRefund(refundId)
        );
    }
}