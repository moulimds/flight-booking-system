package com.example.fare.service.impl;

import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.dto.request.CalculatePriceRequest;
import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.request.UpdateFareRequest;
import com.example.fare.dto.response.CalculatePriceResponse;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.entity.Fare;
import com.example.fare.entity.FareRule;
import com.example.fare.entity.FareStatus;
import com.example.fare.exception.FareNotFoundException;
import com.example.fare.exception.FareValidationException;
import com.example.fare.mapper.FareMapper;
import com.example.fare.messaging.FareMessagePublisher;
import com.example.fare.repository.FareRepository;
import com.example.fare.service.FareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FareServiceImpl implements FareService {

    private final FareRepository fareRepository;
    private final FareMapper fareMapper;
    private final FareMessagePublisher fareMessagePublisher;

    @Override
    @Transactional
    public FareResponse createFare(CreateFareRequest request) {
        log.info("Creating fare for flightId={}, fareCode={}", request.getFlightId(), request.getFareCode());

        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new FareValidationException("validUntil date cannot be before validFrom date");
        }

        Fare fare = fareMapper.toEntity(request);
        FareRule rule = fare.getFareRule();
        fare.setFareRule(null);
        Fare savedFare = fareRepository.save(fare);

        if (rule != null) {
            rule.setFareId(savedFare.getId());
            savedFare.setFareRule(rule);
            savedFare = fareRepository.save(savedFare);
        }

        FareResponse response = fareMapper.toResponse(savedFare);
        fareMessagePublisher.publishFareCreated(response);

        log.info("Successfully created fare with id={}", savedFare.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FareResponse getFareById(Long fareId) {
        log.info("Fetching fare by id={}", fareId);
        Fare fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new FareNotFoundException("Fare not found with id: " + fareId));
        return fareMapper.toResponse(fare);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getFaresByFlightId(Long flightId) {
        log.info("Fetching all fares for flightId={}", flightId);
        return fareRepository.findByFlightId(flightId).stream()
                .map(fareMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getAvailableFaresByFlightId(Long flightId) {
        log.info("Fetching available active fares for flightId={}", flightId);
        return fareRepository.findByFlightIdAndStatusAndAvailableSeatsGreaterThan(flightId, FareStatus.ACTIVE, 0)
                .stream()
                .filter(fare -> {
                    LocalDateTime now = LocalDateTime.now();
                    return !now.isBefore(fare.getValidFrom()) && !now.isAfter(fare.getValidUntil());
                })
                .map(fareMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FareResponse updateFare(Long fareId, UpdateFareRequest request) {
        log.info("Updating fare with id={}", fareId);
        Fare fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new FareNotFoundException("Fare not found with id: " + fareId));

        if (request.getFareCode() != null) fare.setFareCode(request.getFareCode());
        if (request.getFareType() != null) fare.setFareType(request.getFareType());
        if (request.getCabinClass() != null) fare.setCabinClass(request.getCabinClass());
        if (request.getBasePrice() != null) fare.setBasePrice(request.getBasePrice());
        if (request.getTaxAmount() != null) fare.setTaxAmount(request.getTaxAmount());
        if (request.getCurrency() != null) fare.setCurrency(request.getCurrency());
        if (request.getAvailableSeats() != null) fare.setAvailableSeats(request.getAvailableSeats());
        if (request.getValidFrom() != null) fare.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) fare.setValidUntil(request.getValidUntil());

        fare.recalculateTotalPrice();

        if (fare.getFareRule() != null) {
            FareRule rule = fare.getFareRule();
            if (request.getBaggageAllowance() != null) rule.setBaggageAllowance(request.getBaggageAllowance());
            if (request.getCancellationAllowed() != null) rule.setCancellationAllowed(request.getCancellationAllowed());
            if (request.getCancellationFee() != null) rule.setCancellationFee(request.getCancellationFee());
            if (request.getDateChangeAllowed() != null) rule.setDateChangeAllowed(request.getDateChangeAllowed());
            if (request.getDateChangeFee() != null) rule.setDateChangeFee(request.getDateChangeFee());
            if (request.getMealSelectionAllowed() != null) rule.setMealSelectionAllowed(request.getMealSelectionAllowed());
            if (request.getSeatSelectionAllowed() != null) rule.setSeatSelectionAllowed(request.getSeatSelectionAllowed());
            if (request.getRefundAllowed() != null) rule.setRefundAllowed(request.getRefundAllowed());
        }

        Fare updated = fareRepository.save(fare);
        FareResponse response = fareMapper.toResponse(updated);
        fareMessagePublisher.publishFareUpdated(response);
        return response;
    }

    @Override
    @Transactional
    public FareResponse updateFareStatus(Long fareId, FareStatus status) {
        log.info("Updating fare status for fareId={} to {}", fareId, status);
        Fare fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new FareNotFoundException("Fare not found with id: " + fareId));

        fare.setStatus(status);
        Fare updated = fareRepository.save(fare);
        fareMessagePublisher.publishFareStatusChanged(fareId, status.name());
        return fareMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFare(Long fareId) {
        log.info("Soft deleting fare with id={}", fareId);
        Fare fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new FareNotFoundException("Fare not found with id: " + fareId));
        fare.setStatus(FareStatus.INACTIVE);
        fareRepository.save(fare);
        fareMessagePublisher.publishFareStatusChanged(fareId, FareStatus.INACTIVE.name());
    }

    @Override
    @Transactional(readOnly = true)
    public FareValidationResponse validateFare(Long flightId, Long fareId, Integer requiredSeats, String requestId) {
        log.info("Validating fare: requestId={}, flightId={}, fareId={}, requiredSeats={}",
                requestId, flightId, fareId, requiredSeats);

        int seats = (requiredSeats != null && requiredSeats > 0) ? requiredSeats : 1;

        Fare fare = fareRepository.findByIdAndFlightId(fareId, flightId).orElse(null);

        if (fare == null) {
            log.warn("Fare not found for fareId={} and flightId={}", fareId, flightId);
            return FareValidationResponse.builder()
                    .requestId(requestId)
                    .valid(false)
                    .flightId(flightId)
                    .fareId(fareId)
                    .message("Fare not found for specified flight")
                    .build();
        }

        if (fare.getStatus() != FareStatus.ACTIVE) {
            log.warn("Fare {} is not ACTIVE, status={}", fareId, fare.getStatus());
            return FareValidationResponse.builder()
                    .requestId(requestId)
                    .valid(false)
                    .flightId(flightId)
                    .fareId(fareId)
                    .fareCode(fare.getFareCode())
                    .message("Fare is currently " + fare.getStatus())
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fare.getValidFrom()) || now.isAfter(fare.getValidUntil())) {
            log.warn("Fare {} has expired or is not yet valid (validFrom={}, validUntil={})",
                    fareId, fare.getValidFrom(), fare.getValidUntil());
            return FareValidationResponse.builder()
                    .requestId(requestId)
                    .valid(false)
                    .flightId(flightId)
                    .fareId(fareId)
                    .fareCode(fare.getFareCode())
                    .message("Fare has expired or is not yet valid")
                    .build();
        }

        if (fare.getAvailableSeats() < seats) {
            log.warn("Insufficient seats for fare {}: requested={}, available={}",
                    fareId, seats, fare.getAvailableSeats());
            return FareValidationResponse.builder()
                    .requestId(requestId)
                    .valid(false)
                    .flightId(flightId)
                    .fareId(fareId)
                    .fareCode(fare.getFareCode())
                    .availableSeats(fare.getAvailableSeats())
                    .message("Insufficient seats available (Requested: " + seats + ", Available: " + fare.getAvailableSeats() + ")")
                    .build();
        }

        BigDecimal multiplier = BigDecimal.valueOf(seats);
        BigDecimal totalBase = fare.getBasePrice().multiply(multiplier);
        BigDecimal totalTax = fare.getTaxAmount().multiply(multiplier);
        BigDecimal totalFare = fare.getTotalPrice().multiply(multiplier);

        return FareValidationResponse.builder()
                .requestId(requestId)
                .valid(true)
                .flightId(flightId)
                .fareId(fareId)
                .fareCode(fare.getFareCode())
                .fareType(fare.getFareType())
                .cabinClass(fare.getCabinClass())
                .basePrice(totalBase)
                .taxAmount(totalTax)
                .totalPrice(totalFare)
                .currency(fare.getCurrency())
                .availableSeats(fare.getAvailableSeats())
                .message("Fare validated successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getAllFares(Long flightId, FareStatus status) {
        log.info("Fetching fares with flightId={}, status={}", flightId, status);
        List<Fare> fares;
        if (flightId != null && status != null) {
            fares = fareRepository.findByFlightIdAndStatus(flightId, status);
        } else if (flightId != null) {
            fares = fareRepository.findByFlightId(flightId);
        } else {
            fares = fareRepository.findAll();
            if (status != null) {
                fares = fares.stream().filter(f -> f.getStatus() == status).collect(Collectors.toList());
            }
        }
        return fares.stream().map(fareMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CalculatePriceResponse calculatePrice(CalculatePriceRequest request) {
        log.info("Calculating price for fareId={}, passengerCount={}", request.getFareId(), request.getPassengerCount());
        Fare fare = fareRepository.findById(request.getFareId())
                .orElseThrow(() -> new FareNotFoundException("Fare not found with id: " + request.getFareId()));

        int count = (request.getPassengerCount() != null && request.getPassengerCount() > 0) ? request.getPassengerCount() : 1;
        BigDecimal multiplier = BigDecimal.valueOf(count);
        BigDecimal totalBase = fare.getBasePrice().multiply(multiplier);
        BigDecimal totalTax = fare.getTaxAmount().multiply(multiplier);
        BigDecimal grandTotal = fare.getTotalPrice().multiply(multiplier);

        return CalculatePriceResponse.builder()
                .fareId(fare.getId())
                .flightId(fare.getFlightId())
                .fareCode(fare.getFareCode())
                .fareType(fare.getFareType())
                .cabinClass(fare.getCabinClass())
                .passengerCount(count)
                .basePricePerPassenger(fare.getBasePrice())
                .taxPerPassenger(fare.getTaxAmount())
                .totalPerPassenger(fare.getTotalPrice())
                .totalBasePrice(totalBase)
                .totalTax(totalTax)
                .grandTotal(grandTotal)
                .currency(fare.getCurrency())
                .availableSeats(fare.getAvailableSeats())
                .build();
    }
}
