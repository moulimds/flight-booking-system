package com.airline.seat.dto;

import com.airline.seat.entity.Seat;
import com.airline.seat.entity.SeatClass;
import com.airline.seat.entity.SeatInventory;
import com.airline.seat.entity.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SeatResponse {

    private Long seatId;
    private String flightId;
    private String seatNumber;
    private SeatClass seatClass;
    private Boolean isPreferred;
    private BigDecimal basePriceMultiplier;
    private String flightScheduleId;
    private SeatStatus status;
    private String heldByUserId;
    private LocalDateTime holdExpiresAt;
    private String bookingReference;
    private Long version;

    public SeatResponse() {
    }

    public static SeatResponse from(Seat seat, SeatInventory inventory) {
        SeatResponse dto = new SeatResponse();
        dto.setSeatId(seat.getId());
        dto.setFlightId(seat.getFlightId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatClass(seat.getSeatClass());
        dto.setIsPreferred(seat.getIsPreferred());
        dto.setBasePriceMultiplier(seat.getBasePriceMultiplier());

        if (inventory != null) {
            dto.setFlightScheduleId(inventory.getFlightScheduleId());
            dto.setStatus(inventory.getStatus());
            dto.setHeldByUserId(inventory.getHeldByUserId());
            dto.setHoldExpiresAt(inventory.getHoldExpiresAt());
            dto.setBookingReference(inventory.getBookingReference());
            dto.setVersion(inventory.getVersion());
        } else {
            dto.setStatus(SeatStatus.AVAILABLE);
        }
        return dto;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
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

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public Boolean getIsPreferred() {
        return isPreferred;
    }

    public void setIsPreferred(Boolean preferred) {
        isPreferred = preferred;
    }

    public BigDecimal getBasePriceMultiplier() {
        return basePriceMultiplier;
    }

    public void setBasePriceMultiplier(BigDecimal basePriceMultiplier) {
        this.basePriceMultiplier = basePriceMultiplier;
    }

    public String getFlightScheduleId() {
        return flightScheduleId;
    }

    public void setFlightScheduleId(String flightScheduleId) {
        this.flightScheduleId = flightScheduleId;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getHeldByUserId() {
        return heldByUserId;
    }

    public void setHeldByUserId(String heldByUserId) {
        this.heldByUserId = heldByUserId;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
