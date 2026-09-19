package com.flight.invoice.config;

import com.flight.invoice.entity.Invoice;
import com.flight.invoice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class InvoiceDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceDataLoader.class);

    @Bean
    public CommandLineRunner loadSampleInvoices(InvoiceRepository invoiceRepository) {
        return args -> {
            Optional<Invoice> existing = invoiceRepository.findFirstByBookingIdOrderByIdDesc(1L);
            if (existing.isEmpty()) {
                logger.info("Seeding sample invoice INV-1001 into flight_invoice_db...");

                Invoice invoice = new Invoice(
                        null,
                        "INV-1001",
                        1L,
                        1L,
                        1L,
                        "John Customer",
                        new BigDecimal("4500.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("5000.00"),
                        "GENERATED",
                        LocalDateTime.now()
                );

                invoiceRepository.save(invoice);
                logger.info("Successfully seeded sample invoice INV-1001.");
            }
        };
    }
}
