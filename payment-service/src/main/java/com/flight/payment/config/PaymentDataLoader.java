package com.flight.payment.config;

import com.flight.payment.entity.Payment;
import com.flight.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class PaymentDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDataLoader.class);

    @Bean
    public CommandLineRunner loadSamplePayments(PaymentRepository paymentRepository) {
        return args -> {
            Optional<Payment> existing = paymentRepository.findByPaymentReference("PAY1001");
            if (existing.isEmpty()) {
                logger.info("Seeding sample payment PAY1001 into flight_payment_db...");

                Payment payment = new Payment();
                payment.setPaymentReference("PAY1001");
                payment.setBookingId(1L);
                payment.setUserId(1L);
                payment.setAmount(new BigDecimal("5000.00"));
                payment.setCurrency("INR");
                payment.setPaymentMethod("CARD");
                payment.setPaymentStatus("SUCCESS");
                payment.setTransactionDate(LocalDateTime.now());

                paymentRepository.save(payment);
                logger.info("Successfully seeded sample payment PAY1001.");
            }
        };
    }
}
