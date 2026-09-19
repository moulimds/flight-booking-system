package com.flight.notification.service;

import com.flight.notification.dto.NotificationRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfInvoiceGenerator {

    public byte[] generateInvoicePdf(NotificationRequest request) {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        try {
            // Document data
            String bookingRef = request.getBookingReference() != null ? request.getBookingReference() : ("BK" + (request.getBookingId() != null ? request.getBookingId() : "1001"));
            String customerName = request.getCustomerName() != null ? request.getCustomerName() : "Valued Customer";
            String flightNum = request.getFlightNumber() != null ? request.getFlightNumber() : "AI101";
            String flightName = request.getFlightName() != null ? request.getFlightName() : "SkyWings Air";
            String flightId = request.getFlightId() != null ? String.valueOf(request.getFlightId()) : "1";
            String origin = request.getOrigin() != null ? request.getOrigin() : "DEL (Delhi)";
            String destination = request.getDestination() != null ? request.getDestination() : "BOM (Mumbai)";
            String departureDate = request.getDepartureDate() != null ? request.getDepartureDate() : "2026-10-15";
            String departureTime = request.getDepartureTime() != null ? request.getDepartureTime() : "08:30 AM";
            String seatNum = request.getSeatNumber() != null ? request.getSeatNumber() : "1A";
            String seatClass = request.getSeatClass() != null ? request.getSeatClass() : "ECONOMY";
            String invoiceNum = request.getInvoiceNumber() != null ? request.getInvoiceNumber() : ("INV-" + bookingRef);
            String paymentRef = request.getPaymentReference() != null ? request.getPaymentReference() : "PAY1001";
            double amount = request.getAmount() != null ? request.getAmount() : 5400.00;
            String currency = request.getCurrency() != null ? request.getCurrency() : "INR";
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Stream buffer for drawing commands
            StringBuilder stream = new StringBuilder();

            // 1. Header Banner (Deep Navy #1A365D: 0.10, 0.21, 0.36)
            stream.append("0.10 0.21 0.36 rg 0 740 595 102 re f\n");
            
            // Header Text (White)
            stream.append("BT /F2 20 Tf 1 1 1 rg 50 805 Td (SKYWINGS AIRLINES) Tj ET\n");
            stream.append("BT /F1 10 Tf 0.85 0.90 0.95 rg 50 788 Td (E-TICKET RECEIPT & TAX INVOICE) Tj ET\n");
            stream.append("BT /F1 9 Tf 0.85 0.90 0.95 rg 380 805 Td (Generated: ").append(escapePdf(now)).append(") Tj ET\n");
            stream.append("BT /F2 10 Tf 1 0.84 0.0 rg 380 788 Td (OFFICIAL TRAVEL DOCUMENT) Tj ET\n");

            // 2. Invoice Meta Bar (Light Gray #EDF2F7: 0.93, 0.95, 0.97)
            stream.append("0.93 0.95 0.97 rg 40 685 515 40 re f\n");
            stream.append("0.80 0.85 0.90 RG 0.5 w 40 685 515 40 re S\n");
            stream.append("BT /F2 10 Tf 0.1 0.1 0.1 rg 55 702 Td (INVOICE NO: ) Tj /F1 10 Tf (").append(escapePdf(invoiceNum)).append(") Tj ET\n");
            stream.append("BT /F2 10 Tf 0.1 0.1 0.1 rg 230 702 Td (BOOKING REF: ) Tj /F2 11 Tf 0.1 0.3 0.7 rg (").append(escapePdf(bookingRef)).append(") Tj ET\n");
            stream.append("BT /F2 10 Tf 0.1 0.1 0.1 rg 410 702 Td (STATUS: ) Tj /F2 10 Tf 0.1 0.6 0.2 rg (PAID & CONFIRMED) Tj ET\n");

            // 3. Passenger & Booking Details Box
            stream.append("0.98 0.98 0.99 rg 40 515 515 155 re f\n");
            stream.append("0.85 0.88 0.92 RG 1 w 40 515 515 155 re S\n");
            
            // Section Header
            stream.append("0.15 0.30 0.50 rg 40 645 515 25 re f\n");
            stream.append("BT /F2 11 Tf 1 1 1 rg 55 653 Td (PASSENGER & FLIGHT INFORMATION) Tj ET\n");

            // Passenger details row 1
            drawField(stream, 55, 620, "Passenger Name:", customerName, true);
            drawField(stream, 310, 620, "Email / Contact:", request.getRecipient(), false);

            // Flight details row 2
            drawField(stream, 55, 595, "Flight Airline / Name:", flightName, true);
            drawField(stream, 310, 595, "Flight Number / ID:", flightNum + " (ID: " + flightId + ")", true);

            // Route details row 3
            drawField(stream, 55, 570, "Origin / Departure:", origin, false);
            drawField(stream, 310, 570, "Destination / Arrival:", destination, false);

            // Schedule & seat row 4
            drawField(stream, 55, 545, "Departure Schedule:", departureDate + " at " + departureTime, false);
            drawField(stream, 310, 545, "Seat Assigned / Class:", seatNum + " (" + seatClass + ")", true);

            // 4. Financial & Payment Summary Box
            stream.append("0.98 0.98 0.99 rg 40 330 515 165 re f\n");
            stream.append("0.85 0.88 0.92 RG 1 w 40 330 515 165 re S\n");

            // Header
            stream.append("0.15 0.30 0.50 rg 40 470 515 25 re f\n");
            stream.append("BT /F2 11 Tf 1 1 1 rg 55 478 Td (PAYMENT BREAKDOWN & CHARGES) Tj ET\n");

            double baseFare = amount * 0.85;
            double taxFare = amount * 0.15;

            drawField(stream, 55, 445, "Payment Reference:", paymentRef, true);
            drawField(stream, 310, 445, "Payment Gateway / Mode:", "Verified Instant Settlement", false);

            drawField(stream, 55, 420, "Airfare Base Price:", String.format("%.2f %s", baseFare, currency), false);
            drawField(stream, 310, 420, "User / Account ID:", String.valueOf(request.getUserId() != null ? request.getUserId() : 1), false);

            drawField(stream, 55, 395, "Taxes, GST & Airport Fees:", String.format("%.2f %s", taxFare, currency), false);
            drawField(stream, 310, 395, "Payment Status:", "SUCCESSFUL (DELIVERED)", true);

            // Total highlight row
            stream.append("0.90 0.95 0.90 rg 45 345 505 32 re f\n");
            stream.append("0.20 0.60 0.20 RG 1 w 45 345 505 32 re S\n");
            stream.append("BT /F2 12 Tf 0.1 0.4 0.1 rg 55 358 Td (TOTAL AMOUNT PAID:) Tj /F2 14 Tf 0.05 0.35 0.05 rg 380 358 Td (")
                  .append(String.format("%.2f %s", amount, currency)).append(") Tj ET\n");

            // 5. Boarding & Airport Notice Box
            stream.append("0.95 0.97 1.00 rg 40 215 515 95 re f\n");
            stream.append("0.70 0.80 0.95 RG 0.5 w 40 215 515 95 re S\n");
            stream.append("BT /F2 10 Tf 0.15 0.30 0.55 rg 55 288 Td (IMPORTANT PASSENGER GUIDELINES & AIRPORT VERIFICATION:) Tj ET\n");
            stream.append("BT /F1 8.5 Tf 0.25 0.25 0.25 rg 55 272 Td (1. Present this electronic receipt along with a government-issued photo ID at security.) Tj ET\n");
            stream.append("BT /F1 8.5 Tf 0.25 0.25 0.25 rg 55 257 Td (2. Web Check-in opens 48 hours prior and closes 60 minutes before departure time.) Tj ET\n");
            stream.append("BT /F1 8.5 Tf 0.25 0.25 0.25 rg 55 242 Td (3. Standard cabin baggage: 7 kg; Checked-in baggage: 15 kg per ticket.) Tj ET\n");
            stream.append("BT /F1 8.5 Tf 0.25 0.25 0.25 rg 55 227 Td (4. For rescheduling or support, provide Booking Reference: ").append(escapePdf(bookingRef)).append(") Tj ET\n");

            // 6. Security Barcode graphic simulation
            stream.append("0.2 0.2 0.2 rg\n");
            for (int x = 55; x < 540; x += 4) {
                int w = (x % 3 == 0) ? 2 : 1;
                stream.append(x).append(" 160 ").append(w).append(" 35 re f\n");
            }
            stream.append("BT /F1 8 Tf 0.4 0.4 0.4 rg 195 145 Td (*").append(escapePdf(bookingRef)).append("-").append(escapePdf(paymentRef)).append("*) Tj ET\n");

            // 7. Footer
            stream.append("0.80 0.80 0.80 RG 0.5 w 40 125 515 0 re S\n");
            stream.append("BT /F1 8 Tf 0.5 0.5 0.5 rg 110 110 Td (SkyWings Airlines Limited - Customer Care: support@skywings.com | +91-1800-SKY-WINGS) Tj ET\n");
            stream.append("BT /F1 7.5 Tf 0.55 0.55 0.55 rg 135 95 Td (This is a computer-generated tax invoice and requires no physical signature.) Tj ET\n");

            byte[] contentBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

            // Assemble PDF Objects
            List<Integer> offsets = new ArrayList<>();
            pdf.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 1: Catalog
            offsets.add(pdf.size());
            pdf.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 2: Pages
            offsets.add(pdf.size());
            pdf.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 3: Page
            offsets.add(pdf.size());
            pdf.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 4: Font F1 (Helvetica regular)
            offsets.add(pdf.size());
            pdf.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 5: Font F2 (Helvetica bold)
            offsets.add(pdf.size());
            pdf.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 6: Content Stream
            offsets.add(pdf.size());
            String streamHeader = "6 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n";
            pdf.write(streamHeader.getBytes(StandardCharsets.US_ASCII));
            pdf.write(contentBytes);
            pdf.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Xref Table
            int xrefOffset = pdf.size();
            pdf.write(("xref\n0 " + (offsets.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
            pdf.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (int off : offsets) {
                pdf.write(String.format("%010d 00000 n \n", off).getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer
            pdf.write(("trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));

            return pdf.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }
    }

    private void drawField(StringBuilder stream, int x, int y, String label, String value, boolean boldValue) {
        String font = boldValue ? "/F2" : "/F1";
        stream.append("BT /F1 9 Tf 0.40 0.40 0.40 rg ")
              .append(x).append(" ").append(y).append(" Td (")
              .append(escapePdf(label)).append(" ) Tj ")
              .append(font).append(" 9.5 Tf 0.10 0.15 0.20 rg (")
              .append(escapePdf(value != null ? value : "-")).append(") Tj ET\n");
    }

    private String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", " ");
    }
}
