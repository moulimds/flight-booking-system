package com.flight.payment.dto;

public class PaymentProcessRequest {

    private String simulatedStatus = "SUCCESS"; // "SUCCESS" or "FAILED"
    private String failureReason;

    public PaymentProcessRequest() {
    }

    public PaymentProcessRequest(String simulatedStatus, String failureReason) {
        this.simulatedStatus = simulatedStatus;
        this.failureReason = failureReason;
    }

    public String getSimulatedStatus() {
        return simulatedStatus;
    }

    public void setSimulatedStatus(String simulatedStatus) {
        this.simulatedStatus = simulatedStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
