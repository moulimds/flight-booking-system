package com.example.invoice.pdf;

import com.example.invoice.dto.InvoiceResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {


    public byte[] generatePdf(
            InvoiceResponse invoice) {

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();


            Document document =
                    new Document(
                            PageSize.A4,
                            40,
                            40,
                            40,
                            40
                    );


            PdfWriter.getInstance(
                    document,
                    outputStream
            );


            document.open();


            // =============================================
            // TITLE
            // =============================================

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            22
                    );


            Paragraph title =
                    new Paragraph(
                            "FLIGHT BOOKING INVOICE",
                            titleFont
                    );


            title.setAlignment(
                    Element.ALIGN_CENTER
            );


            document.add(title);


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // INVOICE INFORMATION
            // =============================================

            document.add(
                    new Paragraph(
                            "Invoice Number: "
                                    + invoice.invoiceNumber()
                    )
            );


            document.add(
                    new Paragraph(
                            "Invoice ID: "
                                    + invoice.invoiceId()
                    )
            );


            document.add(
                    new Paragraph(
                            "Booking ID: "
                                    + invoice.bookingId()
                    )
            );


            document.add(
                    new Paragraph(
                            "Invoice Status: "
                                    + invoice.invoiceStatus()
                    )
            );


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // CUSTOMER
            // =============================================

            Font headingFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            14
                    );


            document.add(
                    new Paragraph(
                            "Customer Details",
                            headingFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Customer ID: "
                                    + invoice.customerId()
                    )
            );


            document.add(
                    new Paragraph(
                            "Name: "
                                    + invoice.customerName()
                    )
            );


            document.add(
                    new Paragraph(
                            "Email: "
                                    + invoice.customerEmail()
                    )
            );


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // FLIGHT
            // =============================================

            document.add(
                    new Paragraph(
                            "Flight Details",
                            headingFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Flight Number: "
                                    + invoice.flightNumber()
                    )
            );


            document.add(
                    new Paragraph(
                            "From: "
                                    + invoice.source()
                    )
            );


            document.add(
                    new Paragraph(
                            "To: "
                                    + invoice.destination()
                    )
            );


            document.add(
                    new Paragraph(
                            "Travel Date: "
                                    + invoice.travelDate()
                    )
            );


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // FARE TABLE
            // =============================================

            document.add(
                    new Paragraph(
                            "Fare Breakdown",
                            headingFont
                    )
            );


            PdfPTable table =
                    new PdfPTable(2);


            table.setWidthPercentage(100);


            addRow(
                    table,
                    "Base Fare",
                    invoice.baseFare()
                            + " "
                            + invoice.currency()
            );


            addRow(
                    table,
                    "Tax",
                    invoice.taxAmount()
                            + " "
                            + invoice.currency()
            );


            addRow(
                    table,
                    "Service Fee",
                    invoice.serviceFee()
                            + " "
                            + invoice.currency()
            );


            addRow(
                    table,
                    "Discount",
                    invoice.discount()
                            + " "
                            + invoice.currency()
            );


            addRow(
                    table,
                    "TOTAL",
                    invoice.totalAmount()
                            + " "
                            + invoice.currency()
            );


            document.add(table);


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // PAYMENT
            // =============================================

            document.add(
                    new Paragraph(
                            "Payment Details",
                            headingFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Payment ID: "
                                    + safe(
                                    invoice.paymentId()
                            )
                    )
            );


            document.add(
                    new Paragraph(
                            "Transaction ID: "
                                    + safe(
                                    invoice.transactionId()
                            )
                    )
            );


            document.add(
                    new Paragraph(
                            "Payment Method: "
                                    + safe(
                                    invoice.paymentMethod()
                            )
                    )
            );


            document.add(
                    new Paragraph(
                            "Payment Status: "
                                    + safe(
                                    invoice.paymentStatus()
                            )
                    )
            );


            document.add(
                    new Paragraph(" ")
            );


            // =============================================
            // FOOTER
            // =============================================

            Paragraph footer =
                    new Paragraph(
                            "Thank you for booking your flight with us!"
                    );


            footer.setAlignment(
                    Element.ALIGN_CENTER
            );


            document.add(footer);


            document.close();


            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to generate invoice PDF",
                    exception
            );
        }
    }


    private void addRow(
            PdfPTable table,
            String label,
            String value) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(label)
                );


        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(value)
                );


        table.addCell(labelCell);
        table.addCell(valueCell);
    }


    private String safe(String value) {

        return value == null
                ? "-"
                : value;
    }
}