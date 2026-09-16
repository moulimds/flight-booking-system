package com.flightbooking.checkin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String passengerId;

    @Column(nullable = false)
    private String flightId;

    private String seatNumber;
    private Integer baggageCount;
    private Double baggageWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInStatus status;

    @Column(nullable = false)
    private LocalDateTime checkedInAt;

    public Long getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getPassengerId() { return passengerId; }
    public String getFlightId() { return flightId; }
    public String getSeatNumber() { return seatNumber; }
    public Integer getBaggageCount() { return baggageCount; }
    public Double getBaggageWeight() { return baggageWeight; }
    public CheckInStatus getStatus() { return status; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }

    public void setId(Long id) { this.id = id; }
    public void setBookingId(String v) { bookingId = v; }
    public void setPassengerId(String v) { passengerId = v; }
    public void setFlightId(String v) { flightId = v; }
    public void setSeatNumber(String v) { seatNumber = v; }
    public void setBaggageCount(Integer v) { baggageCount = v; }
    public void setBaggageWeight(Double v) { baggageWeight = v; }
    public void setStatus(CheckInStatus v) { status = v; }
    public void setCheckedInAt(LocalDateTime v) { checkedInAt = v; }
}
