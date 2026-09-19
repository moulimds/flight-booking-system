package com.flight.checkin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_in_reference", nullable = false, unique = true, length = 50)
    private String checkInReference;

    @Column(name = "booking_id", nullable = false, length = 50)
    private String bookingId;

    @Column(name = "passenger_id", nullable = false, length = 50)
    private String passengerId;

    @Column(name = "flight_id", nullable = false, length = 50)
    private String flightId;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "baggage_count", nullable = false)
    private Integer baggageCount = 0;

    @Column(name = "baggage_weight", nullable = false)
    private Double baggageWeight = 0.0;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "CHECKED_IN"; // CHECKED_IN, CANCELLED

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "boarding_pass_id", nullable = false, length = 50)
    private String boardingPassId;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CheckIn() {
    }

    public CheckIn(Long id, String checkInReference, String bookingId, String passengerId, String flightId,
                   String seatNumber, Integer baggageCount, Double baggageWeight, String status,
                   LocalDateTime checkedInAt, String boardingPassId, String customerEmail) {
        this.id = id;
        this.checkInReference = checkInReference;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.baggageCount = baggageCount;
        this.baggageWeight = baggageWeight;
        this.status = status;
        this.checkedInAt = checkedInAt;
        this.boardingPassId = boardingPassId;
        this.customerEmail = customerEmail;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.checkedInAt == null) {
            this.checkedInAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCheckInReference() {
        return checkInReference;
    }

    public void setCheckInReference(String checkInReference) {
        this.checkInReference = checkInReference;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getBaggageCount() {
        return baggageCount;
    }

    public void setBaggageCount(Integer baggageCount) {
        this.baggageCount = baggageCount;
    }

    public Double getBaggageWeight() {
        return baggageWeight;
    }

    public void setBaggageWeight(Double baggageWeight) {
        this.baggageWeight = baggageWeight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getBoardingPassId() {
        return boardingPassId;
    }

    public void setBoardingPassId(String boardingPassId) {
        this.boardingPassId = boardingPassId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "id=" + id +
                ", checkInReference='" + checkInReference + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", passengerId='" + passengerId + '\'' +
                ", flightId='" + flightId + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", baggageCount=" + baggageCount +
                ", baggageWeight=" + baggageWeight +
                ", status='" + status + '\'' +
                ", checkedInAt=" + checkedInAt +
                ", boardingPassId='" + boardingPassId + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                '}';
    }
}
