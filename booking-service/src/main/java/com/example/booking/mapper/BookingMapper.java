package com.example.booking.mapper;

import com.example.booking.dto.request.PassengerRequest;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.dto.response.BookingStatusHistoryResponse;
import com.example.booking.dto.response.PassengerResponse;
import com.example.booking.dto.response.PriceBreakdownResponse;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatusHistory;
import com.example.booking.entity.Passenger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public Passenger toPassengerEntity(PassengerRequest request) {
        return Passenger.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .passengerType(request.getPassengerType())
                .passportNumber(request.getPassportNumber())
                .nationality(request.getNationality())
                .seatNumber(request.getSeatNumber())
                .build();
    }

    public PassengerResponse toPassengerResponse(Passenger passenger) {
        if (passenger == null) return null;
        return PassengerResponse.builder()
                .id(passenger.getId())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .dateOfBirth(passenger.getDateOfBirth())
                .gender(passenger.getGender())
                .passengerType(passenger.getPassengerType())
                .passportNumber(passenger.getPassportNumber())
                .nationality(passenger.getNationality())
                .seatNumber(passenger.getSeatNumber())
                .individualBasePrice(passenger.getIndividualBasePrice())
                .individualTaxAmount(passenger.getIndividualTaxAmount())
                .individualTotalAmount(passenger.getIndividualTotalAmount())
                .build();
    }

    public BookingStatusHistoryResponse toHistoryResponse(BookingStatusHistory history) {
        if (history == null) return null;
        return BookingStatusHistoryResponse.builder()
                .id(history.getId())
                .bookingId(history.getBookingId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .reason(history.getReason())
                .changedAt(history.getChangedAt())
                .build();
    }

    public com.example.booking.dto.response.FlightSeatResponse toSeatResponse(com.example.booking.entity.FlightSeat seat) {
        if (seat == null) return null;
        return com.example.booking.dto.response.FlightSeatResponse.builder()
                .id(seat.getId())
                .flightId(seat.getFlightId())
                .seatNumber(seat.getSeatNumber())
                .cabinClass(seat.getCabinClass())
                .seatStatus(seat.getSeatStatus())
                .isAvailable(seat.isCurrentlyAvailable())
                .bookingId(seat.getBookingId())
                .userId(seat.getUserId())
                .holdExpiresAt(seat.getHoldExpiresAt())
                .blockedReason(seat.getBlockedReason())
                .build();
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) return null;

        List<PassengerResponse> passengerResponses = (booking.getPassengers() != null)
                ? booking.getPassengers().stream()
                .map(this::toPassengerResponse)
                .collect(Collectors.toList())
                : Collections.emptyList();

        PriceBreakdownResponse breakdown = null;
        if (booking.getAdultCount() != null && booking.getAdultCount() > 0 ||
            booking.getChildCount() != null && booking.getChildCount() > 0 ||
            booking.getInfantCount() != null && booking.getInfantCount() > 0) {

            BigDecimal base = booking.getBasePrice();
            BigDecimal tax = booking.getTaxAmount();

            breakdown = PriceBreakdownResponse.builder()
                    .adult(PriceBreakdownResponse.MemberPricingDetail.builder()
                            .count(booking.getAdultCount() != null ? booking.getAdultCount() : 0)
                            .subtotal(booking.getAdultTotal())
                            .build())
                    .child(PriceBreakdownResponse.MemberPricingDetail.builder()
                            .count(booking.getChildCount() != null ? booking.getChildCount() : 0)
                            .subtotal(booking.getChildTotal())
                            .build())
                    .infant(PriceBreakdownResponse.MemberPricingDetail.builder()
                            .count(booking.getInfantCount() != null ? booking.getInfantCount() : 0)
                            .subtotal(booking.getInfantTotal())
                            .build())
                    .grandTotal(booking.getTotalAmount())
                    .currency(booking.getCurrency())
                    .build();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .pnr(booking.getPnr())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .fareId(booking.getFareId())
                .fareCode(booking.getFareCode())
                .fareType(booking.getFareType())
                .cabinClass(booking.getCabinClass())
                .basePrice(booking.getBasePrice())
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .cancellationFee(booking.getCancellationFee())
                .refundAmount(booking.getRefundAmount())
                .cancellationReason(booking.getCancellationReason())
                .holdExpiresAt(booking.getHoldExpiresAt())
                .blockedReason(booking.getBlockedReason())
                .priceBreakdown(breakdown)
                .passengers(passengerResponses)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .version(booking.getVersion())
                .build();
    }
}
