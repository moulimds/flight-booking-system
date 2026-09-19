package com.flight.fare.service;

import com.flight.fare.dto.FareRequest;
import com.flight.fare.dto.FareResponse;
import com.flight.fare.entity.Fare;
import com.flight.fare.exception.InvalidFareException;
import com.flight.fare.exception.ResourceNotFoundException;
import com.flight.fare.repository.FareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FareServiceImpl implements FareService {

    private static final Logger logger = LoggerFactory.getLogger(FareServiceImpl.class);

    @Autowired
    private FareRepository fareRepository;

    @Override
    public FareResponse createFare(FareRequest request) {
        logger.info("Creating fare for flight id: {}", request.getFlightId());

        validateFareRequest(request);

        BigDecimal finalFare = calculateFinalFare(request.getBaseFare(), request.getTax(), request.getDiscount());

        Optional<Fare> existingOpt = fareRepository.findFirstByFlightIdOrderByIdDesc(request.getFlightId());
        Fare fare = existingOpt.orElse(new Fare());
        fare.setFlightId(request.getFlightId());
        fare.setBaseFare(request.getBaseFare());
        fare.setTax(request.getTax());
        fare.setDiscount(request.getDiscount());
        fare.setFinalFare(finalFare);
        fare.setCurrency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR");

        Fare savedFare = fareRepository.save(fare);
        return mapToFareResponse(savedFare);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareResponse> getAllFares() {
        return fareRepository.findAll().stream()
                .map(this::mapToFareResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FareResponse getFareById(Long id) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found with id: " + id));
        return mapToFareResponse(fare);
    }

    @Override
    @Transactional(readOnly = true)
    public FareResponse getFareByFlightId(Long flightId) {
        Fare fare = fareRepository.findFirstByFlightIdOrderByIdDesc(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for flight id: " + flightId));
        return mapToFareResponse(fare);
    }

    @Override
    public FareResponse updateFare(Long id, FareRequest request) {
        logger.info("Updating fare with id: {}", id);

        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found with id: " + id));

        validateFareRequest(request);

        BigDecimal finalFare = calculateFinalFare(request.getBaseFare(), request.getTax(), request.getDiscount());

        fare.setFlightId(request.getFlightId());
        fare.setBaseFare(request.getBaseFare());
        fare.setTax(request.getTax());
        fare.setDiscount(request.getDiscount());
        fare.setFinalFare(finalFare);
        if (request.getCurrency() != null) {
            fare.setCurrency(request.getCurrency().toUpperCase());
        }

        Fare updatedFare = fareRepository.save(fare);
        return mapToFareResponse(updatedFare);
    }

    @Override
    public void deleteFare(Long id) {
        logger.info("Deleting fare with id: {}", id);
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found with id: " + id));
        fareRepository.delete(fare);
    }

    private void validateFareRequest(FareRequest request) {
        if (request.getBaseFare().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFareException("Base fare cannot be negative");
        }
        if (request.getTax().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFareException("Tax cannot be negative");
        }
        if (request.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFareException("Discount cannot be negative");
        }
        BigDecimal calculated = request.getBaseFare().add(request.getTax()).subtract(request.getDiscount());
        if (calculated.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFareException("Final calculated fare cannot be negative");
        }
    }

    private BigDecimal calculateFinalFare(BigDecimal baseFare, BigDecimal tax, BigDecimal discount) {
        return baseFare.add(tax).subtract(discount);
    }

    private FareResponse mapToFareResponse(Fare fare) {
        return new FareResponse(
                fare.getId(),
                fare.getFlightId(),
                fare.getBaseFare(),
                fare.getTax(),
                fare.getDiscount(),
                fare.getFinalFare(),
                fare.getCurrency(),
                fare.getCreatedAt(),
                fare.getUpdatedAt()
        );
    }
}
