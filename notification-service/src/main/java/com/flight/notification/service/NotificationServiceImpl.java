package com.flight.notification.service;

import com.flight.notification.dto.NotificationRequest;
import com.flight.notification.dto.NotificationResponse;
import com.flight.notification.entity.Notification;
import com.flight.notification.exception.ResourceNotFoundException;
import com.flight.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Autowired
    private PdfInvoiceGenerator pdfInvoiceGenerator;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:sindhusakthi41@gmail.com}")
    private String fromEmail;

    @org.springframework.beans.factory.annotation.Value("${app.default-notification-email:sindhusakthi41@gmail.com}")
    private String defaultEmail;

    // Strict allowed email events set as requested
    private static final Set<String> ALLOWED_EMAIL_EVENTS = new HashSet<>(Arrays.asList(
            "REGISTRATION",
            "BOOKING_SUCCESS",
            "CANCELLATION",
            "PAYMENT_SUCCESS",
            "PAYMENT_FAILURE",
            "BILL_INVOICE",
            "REFUND_INITIALIZATION",
            "REFUND_SUCCESS",
            "REFUND_PROCESSING",
            "PASSWORD_CHANGE",
            "PASSWORD_RESET_OTP",
            "RESCHEDULING"
    ));

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        String targetRecipient = request.getRecipient();
        if (targetRecipient == null || targetRecipient.trim().isEmpty() || targetRecipient.endsWith("@example.com") || targetRecipient.endsWith("@flight.com")) {
            targetRecipient = defaultEmail;
        }

        String eventCategory = resolveEventCategory(request);
        boolean isEmailAllowed = ALLOWED_EMAIL_EVENTS.contains(eventCategory);

        logger.info("Processing notification. EventCategory: '{}' | Recipient: {} | Subject: {} | EmailAllowed: {}",
                eventCategory, targetRecipient, request.getSubject(), isEmailAllowed);

        String emailStatus = "SAVED_NO_DISPATCH";

        // ONLY send email notification when the event matches the allowed list
        if (isEmailAllowed && mailSender != null) {
            try {
                jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper =
                        new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromEmail, "SkyWings Airlines");
                helper.setTo(targetRecipient);
                if (!targetRecipient.equalsIgnoreCase(defaultEmail)) {
                    helper.setCc(defaultEmail);
                }

                String formattedSubject = formatSubject(eventCategory, request);
                helper.setSubject(formattedSubject);

                // Build tailored HTML email body for this exact event
                String htmlBody = buildTailoredHtmlBody(eventCategory, request);
                helper.setText(htmlBody, true);

                // Attach PDF where appropriate (Booking Success, Payment Success, Invoice, Reschedule)
                boolean attachPdf = Boolean.TRUE.equals(request.getAttachPdf())
                        || "BOOKING_SUCCESS".equals(eventCategory)
                        || "PAYMENT_SUCCESS".equals(eventCategory)
                        || "BILL_INVOICE".equals(eventCategory)
                        || "RESCHEDULING".equals(eventCategory);

                if (attachPdf) {
                    try {
                        byte[] pdfBytes = pdfInvoiceGenerator.generateInvoicePdf(request);
                        String docRef = request.getInvoiceNumber() != null ? request.getInvoiceNumber() :
                                       (request.getBookingReference() != null ? request.getBookingReference() : "BK1001");
                        String filename = "SkyWings_Document_" + docRef + ".pdf";
                        helper.addAttachment(filename, new org.springframework.core.io.ByteArrayResource(pdfBytes));
                        logger.info("Attached downloadable PDF document '{}' to email for {}", filename, targetRecipient);
                    } catch (Exception pdfEx) {
                        logger.warn("Could not generate PDF attachment: {}", pdfEx.getMessage());
                    }
                }

                mailSender.send(mimeMessage);
                logger.info("Real email notification successfully delivered for event '{}' to {} via Gmail SMTP", eventCategory, targetRecipient);
                emailStatus = "DELIVERED";
            } catch (Exception ex) {
                logger.warn("Could not dispatch email via SMTP to {}: {}", targetRecipient, ex.getMessage());
                emailStatus = "QUEUED_SIMULATED";
            }
        } else if (!isEmailAllowed) {
            logger.info("Event category '{}' is excluded from email push notifications per policy.", eventCategory);
            emailStatus = "SKIPPED_NOT_IN_EMAIL_POLICY";
        }

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setBookingId(request.getBookingId());
        notification.setNotificationType(request.getNotificationType() != null ? request.getNotificationType().toUpperCase() : "EMAIL");
        notification.setRecipient(targetRecipient);
        notification.setSubject(request.getSubject() != null ? request.getSubject() : eventCategory);
        notification.setMessage(request.getMessage() != null ? request.getMessage() : ("Notification for " + eventCategory));
        notification.setStatus(emailStatus);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return mapToNotificationResponse(saved);
    }

    private String resolveEventCategory(NotificationRequest req) {
        String subj = req.getSubject() != null ? req.getSubject().toUpperCase() : "";
        String msg = req.getMessage() != null ? req.getMessage().toUpperCase() : "";
        String type = req.getNotificationType() != null ? req.getNotificationType().toUpperCase() : "";

        if (subj.contains("REGISTER") || subj.contains("REGISTRATION") || msg.contains("REGISTRATION")) return "REGISTRATION";
        if (subj.contains("PASSWORD CHANGE") || subj.contains("PASSWORD HAS BEEN CHANGED") || msg.contains("PASSWORD CHANGE")) return "PASSWORD_CHANGE";
        if (subj.contains("OTP") || subj.contains("PASSWORD RESET") || msg.contains("OTP")) return "PASSWORD_RESET_OTP";
        if (subj.contains("RESCHEDUL") || msg.contains("RESCHEDUL")) return "RESCHEDULING";
        if (subj.contains("CANCEL") || msg.contains("CANCEL")) return "CANCELLATION";
        if (subj.contains("PAYMENT FAIL") || msg.contains("PAYMENT FAIL") || subj.contains("PAYMENT DECLINED")) return "PAYMENT_FAILURE";
        if (subj.contains("PAYMENT SUCCESS") || msg.contains("PAYMENT SUCCESS") || subj.contains("PAYMENT COMPLETED")) return "PAYMENT_SUCCESS";
        if (subj.contains("INVOICE") || subj.contains("BILL") || msg.contains("INVOICE")) return "BILL_INVOICE";
        if (subj.contains("REFUND INIT") || msg.contains("REFUND INIT")) return "REFUND_INITIALIZATION";
        if (subj.contains("REFUND SUCCESS") || msg.contains("REFUND SUCCESS") || msg.contains("CREDITED")) return "REFUND_SUCCESS";
        if (subj.contains("BOOKING CONFIRM") || subj.contains("BOOKING SUCCESS") || subj.contains("BOOKING CREAT")
                || msg.contains("BOOKING CONFIRM") || msg.contains("BOOKING SUCCESS") || msg.contains("BOOKING IS CONFIRMED")
                || msg.contains("IS SUCCESSFULLY CONFIRMED") || msg.contains("BOOKING HAS BEEN CONFIRMED")) {
            return "BOOKING_SUCCESS";
        }

        return "OTHER";
    }

    private String formatSubject(String category, NotificationRequest req) {
        if (req.getSubject() != null && !req.getSubject().trim().isEmpty() && !req.getSubject().startsWith("Flight Booking Notification:")) {
            return req.getSubject();
        }
        String ref = req.getBookingReference() != null ? req.getBookingReference() : "BK1001";
        switch (category) {
            case "REGISTRATION": return "Welcome to SkyWings - Customer Registration Successful";
            case "BOOKING_SUCCESS": return "Booking Confirmed: " + ref + " - SkyWings Airlines";
            case "CANCELLATION": return "Cancellation Notice & Refund Details: " + ref;
            case "PAYMENT_SUCCESS": return "Payment Successful: " + (req.getPaymentReference() != null ? req.getPaymentReference() : "PAY1001");
            case "PAYMENT_FAILURE": return "Payment Failed Alert: Booking " + ref;
            case "BILL_INVOICE": return "Official Tax Invoice & Travel Document: " + (req.getInvoiceNumber() != null ? req.getInvoiceNumber() : "INV-2026-991");
            case "REFUND_INITIALIZATION": return "Refund Initialized for Booking " + ref;
            case "REFUND_SUCCESS": return "Refund Successful - Credited: " + ref;
            case "REFUND_PROCESSING": return "Refund Processing with Bank: " + ref;
            case "PASSWORD_CHANGE": return "Security Alert: Password Changed Successfully";
            case "PASSWORD_RESET_OTP": return "Your SkyWings Password Reset OTP";
            case "RESCHEDULING": return "Flight Rescheduled: " + ref + " - SkyWings Airlines";
            default: return "SkyWings Airlines Notification";
        }
    }

    private String buildTailoredHtmlBody(String eventCategory, NotificationRequest req) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a"));
        String customerName = req.getCustomerName() != null ? req.getCustomerName() : "Sindhu Sakthivel";
        String bookingRef = req.getBookingReference() != null ? req.getBookingReference() : (req.getBookingId() != null ? "BK" + req.getBookingId() : "BK1001");
        String flightNum = req.getFlightNumber() != null ? req.getFlightNumber() : "AI101";
        String airline = req.getFlightName() != null ? req.getFlightName() : "Air India";
        String origin = req.getOrigin() != null ? req.getOrigin() : "DEL (Delhi - Terminal 3)";
        String dest = req.getDestination() != null ? req.getDestination() : "BOM (Mumbai - Terminal 2)";
        String depDate = req.getDepartureDate() != null ? req.getDepartureDate() : "17-Sep-2026";
        String depTime = req.getDepartureTime() != null ? req.getDepartureTime() : "05:50 PM";
        String arrDate = req.getArrivalDate() != null ? req.getArrivalDate() : depDate;
        String arrTime = req.getArrivalTime() != null ? req.getArrivalTime() : "08:15 PM";
        String seatNum = req.getSeatNumber() != null ? req.getSeatNumber() : "14A";
        int paxCount = req.getPassengerCount() != null ? req.getPassengerCount() : 1;
        double totalAmt = req.getTotalAmount() != null ? req.getTotalAmount() : (req.getAmount() != null ? req.getAmount() : 5400.00);
        String currency = req.getCurrency() != null ? req.getCurrency() : "INR";
        String paymentRef = req.getPaymentReference() != null ? req.getPaymentReference() : (req.getPaymentId() != null ? req.getPaymentId() : "PAY1001");
        String invoiceNum = req.getInvoiceNumber() != null ? req.getInvoiceNumber() : "INV-2026-991";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>")
          .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 20px; color: #2d3748; }")
          .append(".card { max-width: 620px; margin: 0 auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,0.08); border: 1px solid #e2e8f0; }")
          .append(".header { background: linear-gradient(135deg, #1a365d 0%, #2b6cb0 100%); padding: 22px 28px; color: #ffffff; }")
          .append(".header h1 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: 0.5px; }")
          .append(".header p { margin: 4px 0 0 0; font-size: 12px; color: #e2e8f0; }")
          .append(".content { padding: 24px 28px; }")
          .append(".badge { display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 11px; font-weight: 700; text-transform: uppercase; margin-bottom: 16px; }")
          .append(".badge-green { background: #c6f6d5; color: #22543d; }")
          .append(".badge-red { background: #fed7d7; color: #742a2a; }")
          .append(".badge-blue { background: #bee3f8; color: #2a4365; }")
          .append(".badge-amber { background: #feebc8; color: #744210; }")
          .append(".info-table { width: 100%; border-collapse: collapse; margin-top: 12px; margin-bottom: 18px; font-size: 13px; }")
          .append(".info-table td { padding: 9px 12px; border-bottom: 1px solid #edf2f7; }")
          .append(".info-table tr:last-child td { border-bottom: none; }")
          .append(".lbl { color: #718096; width: 42%; font-weight: 500; }")
          .append(".val { color: #1a202c; font-weight: 600; }")
          .append(".val-highlight { color: #2b6cb0; font-weight: 700; }")
          .append(".val-amount { color: #2f855a; font-weight: 700; font-size: 14px; }")
          .append(".alert-box { background: #ebf8ff; border-left: 4px solid #3182ce; padding: 12px 16px; border-radius: 4px; font-size: 13px; margin-bottom: 16px; line-height: 1.5; color: #2b6cb0; }")
          .append(".alert-warn { background: #fffaf0; border-left: 4px solid #dd6b20; color: #7b341e; }")
          .append(".pdf-bar { background: #f0fff4; border: 1px dashed #38a169; border-radius: 6px; padding: 12px 16px; margin-top: 16px; font-size: 12.5px; color: #22543d; }")
          .append(".footer { background: #f7fafc; padding: 16px 28px; text-align: center; font-size: 11px; color: #a0aec0; border-top: 1px solid #edf2f7; }")
          .append("</style></head><body><div class='card'>")
          .append("<div class='header'><h1>SKYWINGS AIRLINES</h1><p>Official Transaction &amp; Security Notification</p></div>")
          .append("<div class='content'>");

        switch (eventCategory) {
            case "REGISTRATION":
                String email = req.getRecipient() != null ? req.getRecipient() : "sindhu@example.com";
                String username = req.getUsername() != null ? req.getUsername() : "sindhu123";
                String regTime = req.getRegistrationDateTime() != null ? req.getRegistrationDateTime() : now;
                String accStatus = req.getAccountStatus() != null ? req.getAccountStatus() : "ACTIVE / PENDING_VERIFICATION";
                String verifyLink = req.getVerificationLink() != null ? req.getVerificationLink() : "482913 or https://skywings.com/verify?otp=482913";
                String otpExp = req.getOtpExpiry() != null ? req.getOtpExpiry() : "10 minutes";
                String custId = req.getCustomerId() != null ? req.getCustomerId() : "CUST10045";
                String secMsg = req.getSecurityMessage() != null ? req.getSecurityMessage() : "Don't share OTP/password with anyone.";

                sb.append("<span class='badge badge-green'>Registration Successful</span>")
                  .append("<div class='alert-box'>Welcome aboard, <strong>").append(customerName).append("</strong>! Your account registration has succeeded.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Email:</td><td class='val'>").append(email).append("</td></tr>")
                  .append("<tr><td class='lbl'>Username:</td><td class='val'>").append(username).append("</td></tr>")
                  .append("<tr><td class='lbl'>Registration Date &amp; Time:</td><td class='val'>").append(regTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Account Status:</td><td class='val'>").append(accStatus).append("</td></tr>")
                  .append("<tr><td class='lbl'>Verification link/OTP:</td><td class='val val-highlight'>").append(verifyLink).append("</td></tr>")
                  .append("<tr><td class='lbl'>OTP Expiry:</td><td class='val'>").append(otpExp).append("</td></tr>")
                  .append("<tr><td class='lbl'>Customer ID:</td><td class='val'>").append(custId).append("</td></tr>")
                  .append("<tr><td class='lbl'>Security Message:</td><td class='val' style='color:#c53030;'>").append(secMsg).append("</td></tr>")
                  .append("</table>");
                break;

            case "BOOKING_SUCCESS":
                sb.append("<span class='badge badge-green'>Booking Confirmed</span>")
                  .append("<div class='alert-box'>Great news! Your booking is confirmed. Your itinerary details are below:</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight ID:</td><td class='val'>").append(req.getFlightId() != null ? req.getFlightId() : 1).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight Number:</td><td class='val'>").append(flightNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Airline:</td><td class='val'>").append(airline).append("</td></tr>")
                  .append("<tr><td class='lbl'>From (Origin):</td><td class='val'>").append(origin).append("</td></tr>")
                  .append("<tr><td class='lbl'>To (Destination):</td><td class='val'>").append(dest).append("</td></tr>")
                  .append("<tr><td class='lbl'>Departure Date/Time:</td><td class='val'>").append(depDate).append(" at ").append(depTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Arrival Date/Time:</td><td class='val'>").append(arrDate).append(" at ").append(arrTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Seat Number:</td><td class='val'>").append(seatNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Passenger Count:</td><td class='val'>").append(paxCount).append("</td></tr>")
                  .append("<tr><td class='lbl'>Total Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", totalAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking Status:</td><td class='val val-highlight'>").append(req.getBookingStatus() != null ? req.getBookingStatus() : "CONFIRMED").append("</td></tr>")
                  .append("</table>")
                  .append("<div class='pdf-bar'><strong>&#128196; Downloadable PDF Invoice Attached</strong><br/>An official electronic invoice and boarding itinerary PDF is attached to this email.</div>");
                break;

            case "CANCELLATION":
                double refundAmt = req.getRefundAmount() != null ? req.getRefundAmount() : 4320.00;
                double cancelFee = req.getCancellationFee() != null ? req.getCancellationFee() : (totalAmt - refundAmt);
                String cancelReason = req.getCancellationReason() != null ? req.getCancellationReason() : (req.getReason() != null ? req.getReason() : "Customer Requested Cancellation");
                String cancelTime = req.getCancelledDateTime() != null ? req.getCancelledDateTime() : now;

                sb.append("<span class='badge badge-red'>Booking Cancelled</span>")
                  .append("<div class='alert-box' style='background:#fff5f5; border-color:#e53e3e; color:#742a2a;'>Your booking has been cancelled. Your refund details under our Tiered Refund Policy are outlined below:</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight Number:</td><td class='val'>").append(flightNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>From:</td><td class='val'>").append(origin).append("</td></tr>")
                  .append("<tr><td class='lbl'>To:</td><td class='val'>").append(dest).append("</td></tr>")
                  .append("<tr><td class='lbl'>Travel Date:</td><td class='val'>").append(depDate).append("</td></tr>")
                  .append("<tr><td class='lbl'>Cancelled Date/Time:</td><td class='val'>").append(cancelTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Cancellation Reason:</td><td class='val'>").append(cancelReason).append("</td></tr>")
                  .append("<tr><td class='lbl'>Cancellation Fee:</td><td class='val'>").append(String.format("%.2f %s", cancelFee, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", refundAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Cancellation Status:</td><td class='val val-highlight'>").append(req.getCancellationStatus() != null ? req.getCancellationStatus() : "CANCELLED / REFUND_SCHEDULED").append("</td></tr>")
                  .append("</table>");
                break;

            case "PAYMENT_SUCCESS":
                sb.append("<span class='badge badge-green'>💳 Payment Success</span>")
                  .append("<div class='alert-box'>Your payment was received and settled successfully.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment ID:</td><td class='val'>").append(paymentRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", totalAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment Method:</td><td class='val'>").append(req.getPaymentMethod() != null ? req.getPaymentMethod() : "CARD / UPI Instant").append("</td></tr>")
                  .append("<tr><td class='lbl'>Transaction Date/Time:</td><td class='val'>").append(req.getTransactionDateTime() != null ? req.getTransactionDateTime() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight Number:</td><td class='val'>").append(flightNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment Status:</td><td class='val-amount'>").append(req.getPaymentStatus() != null ? req.getPaymentStatus() : "SUCCESS").append("</td></tr>")
                  .append("</table>")
                  .append("<div class='pdf-bar'><strong>&#128196; PDF Tax Invoice Attached</strong><br/>Download the attached tax receipt for your financial records.</div>");
                break;

            case "PAYMENT_FAILURE":
                sb.append("<span class='badge badge-red'>Payment Failure</span>")
                  .append("<div class='alert-box' style='background:#fff5f5; border-color:#e53e3e; color:#742a2a;'>Your transaction could not be completed. Your booking is held temporarily.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment ID / Trans ID:</td><td class='val'>").append(paymentRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", totalAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment Method:</td><td class='val'>").append(req.getPaymentMethod() != null ? req.getPaymentMethod() : "Online Payment Gateway").append("</td></tr>")
                  .append("<tr><td class='lbl'>Failure Reason:</td><td class='val' style='color:#c53030;'>").append(req.getReason() != null ? req.getReason() : "Transaction declined by issuing bank").append("</td></tr>")
                  .append("<tr><td class='lbl'>Transaction Date/Time:</td><td class='val'>").append(req.getTransactionDateTime() != null ? req.getTransactionDateTime() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment Status:</td><td class='val' style='color:#e53e3e;'>FAILED</td></tr>")
                  .append("</table>");
                break;

            case "BILL_INVOICE":
                double base = req.getBaseFare() != null ? req.getBaseFare() : (totalAmt * 0.85);
                double tax = req.getTaxes() != null ? req.getTaxes() : (totalAmt * 0.15);
                double conv = req.getConvenienceFee() != null ? req.getConvenienceFee() : 0.0;
                double other = req.getOtherCharges() != null ? req.getOtherCharges() : 0.0;
                double disc = req.getDiscount() != null ? req.getDiscount() : 0.0;
                String billAddr = req.getBillingAddress() != null ? req.getBillingAddress() : (req.getRecipient() != null ? req.getRecipient() : "sindhu@example.com");

                sb.append("<span class='badge badge-blue'>Official Tax Invoice</span>")
                  .append("<div class='alert-box'>Your computerized tax invoice has been generated for flight travel.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Billing Address / Email:</td><td class='val'>").append(billAddr).append("</td></tr>")
                  .append("<tr><td class='lbl'>Invoice Number:</td><td class='val val-highlight'>").append(invoiceNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight Number:</td><td class='val'>").append(flightNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Passenger Details:</td><td class='val'>").append(customerName).append(" (Seat ").append(seatNum).append(")</td></tr>")
                  .append("<tr><td class='lbl'>Base Fare:</td><td class='val'>").append(String.format("%.2f %s", base, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Taxes (GST):</td><td class='val'>").append(String.format("%.2f %s", tax, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Convenience Fee:</td><td class='val'>").append(String.format("%.2f %s", conv, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Other Charges:</td><td class='val'>").append(String.format("%.2f %s", other, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Discount:</td><td class='val'>").append(String.format("-%.2f %s", disc, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Total Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", totalAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment Status:</td><td class='val-amount'>PAID</td></tr>")
                  .append("<tr><td class='lbl'>Invoice Date:</td><td class='val'>").append(req.getInvoiceDate() != null ? req.getInvoiceDate() : now).append("</td></tr>")
                  .append("</table>")
                  .append("<div class='pdf-bar'><strong>&#128196; Downloadable PDF Invoice Attached</strong><br/>Download the official signed electronic invoice from the attachment.</div>");
                break;

            case "REFUND_INITIALIZATION":
                String refIdInit = req.getRefundId() != null ? req.getRefundId() : "REF1001";
                double origAmt = req.getOriginalPaymentAmount() != null ? req.getOriginalPaymentAmount() : totalAmt;
                double refAmtInit = req.getRefundAmount() != null ? req.getRefundAmount() : (origAmt * 0.80);

                sb.append("<span class='badge badge-amber'>Refund Initialized</span>")
                  .append("<div class='alert-box alert-warn'>Your refund request has been initiated and queued for banking settlement.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Payment ID:</td><td class='val'>").append(paymentRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund ID:</td><td class='val val-highlight'>").append(refIdInit).append("</td></tr>")
                  .append("<tr><td class='lbl'>Original Payment Amount:</td><td class='val'>").append(String.format("%.2f %s", origAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", refAmtInit, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Reason:</td><td class='val'>").append(req.getReason() != null ? req.getReason() : "Voluntary flight cancellation within 80% refund window").append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Initiated Date/Time:</td><td class='val'>").append(req.getRefundInitiatedDate() != null ? req.getRefundInitiatedDate() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Status:</td><td class='val val-highlight'>INITIALIZED</td></tr>")
                  .append("</table>");
                break;

            case "REFUND_SUCCESS":
                String refIdSucc = req.getRefundId() != null ? req.getRefundId() : "REF1001";
                double refAmtSucc = req.getRefundAmount() != null ? req.getRefundAmount() : 4320.00;
                String creditInfo = req.getExpectedCreditInfo() != null ? req.getExpectedCreditInfo() : "Credited directly to original payment method / card ending in 4111";

                sb.append("<span class='badge badge-green'>Refund Success</span>")
                  .append("<div class='alert-box'>Your refund has been successfully settled and credited back to your account.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund ID:</td><td class='val val-highlight'>").append(refIdSucc).append("</td></tr>")
                  .append("<tr><td class='lbl'>Original Transaction ID:</td><td class='val'>").append(req.getOriginalTransactionId() != null ? req.getOriginalTransactionId() : paymentRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", refAmtSucc, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Completion Date/Time:</td><td class='val'>").append(req.getRefundCompletionDate() != null ? req.getRefundCompletionDate() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Status:</td><td class='val-amount'>COMPLETED / CREDITED</td></tr>")
                  .append("<tr><td class='lbl'>Expected Credit Information:</td><td class='val'>").append(creditInfo).append("</td></tr>")
                  .append("</table>");
                break;

            case "REFUND_PROCESSING":
                String refIdProc = req.getRefundId() != null ? req.getRefundId() : "REF1001";
                double refAmtProc = req.getRefundAmount() != null ? req.getRefundAmount() : 4320.00;
                String timeframe = req.getExpectedTimeframe() != null ? req.getExpectedTimeframe() : "3 to 5 business days";

                sb.append("<span class='badge badge-amber'>Refund Processing</span>")
                  .append("<div class='alert-box alert-warn'>Your refund is currently being processed by our banking partner.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund ID:</td><td class='val val-highlight'>").append(refIdProc).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", refAmtProc, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Refund Initiated Date:</td><td class='val'>").append(req.getRefundInitiatedDate() != null ? req.getRefundInitiatedDate() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Current Refund Status:</td><td class='val val-highlight'>PROCESSING_WITH_BANK</td></tr>")
                  .append("<tr><td class='lbl'>Expected Processing Timeframe:</td><td class='val'>").append(timeframe).append("</td></tr>")
                  .append("</table>");
                break;

            case "PASSWORD_CHANGE":
                String changeTime = req.getChangeDateTime() != null ? req.getChangeDateTime() : now;
                String userIdentifier = req.getUsername() != null ? req.getUsername() : (req.getRecipient() != null ? req.getRecipient() : "sindhu@example.com");
                String devInfo = req.getDeviceInfo() != null ? req.getDeviceInfo() : "Chrome / Windows 11 (IP: 192.168.1.10)";
                String secNotice = req.getSecurityMessage() != null ? req.getSecurityMessage() : "Your password was successfully updated. If you did not perform this action, please reset your password immediately or contact SkyWings Security Support.";

                sb.append("<span class='badge badge-red'>Security Notice: Password Changed</span>")
                  .append("<div class='alert-box alert-warn'>Your SkyWings account password has been updated.</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Email / Username:</td><td class='val'>").append(userIdentifier).append("</td></tr>")
                  .append("<tr><td class='lbl'>Change Date &amp; Time:</td><td class='val'>").append(changeTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Device / IP Information:</td><td class='val'>").append(devInfo).append("</td></tr>")
                  .append("<tr><td class='lbl'>Security Message:</td><td class='val' style='color:#c53030;'>").append(secNotice).append("</td></tr>")
                  .append("</table>")
                  .append("<p style='font-size:12px; color:#718096;'>To reset or change your password anytime via mail, use our official link: <a href='https://skywings.com/auth/reset-password'>https://skywings.com/auth/reset-password</a></p>");
                break;

            case "PASSWORD_RESET_OTP":
                String otpCode = req.getOtp() != null ? req.getOtp() : "784912";
                String userOtpId = req.getUsername() != null ? req.getUsername() : (req.getRecipient() != null ? req.getRecipient() : "sindhu@example.com");
                String reqTime = req.getRequestDateTime() != null ? req.getRequestDateTime() : now;
                String secWarn = req.getSecurityWarning() != null ? req.getSecurityWarning() : "Do not share this OTP with anyone. SkyWings representatives will never ask for your password or OTP.";

                sb.append("<span class='badge badge-blue'>Password Reset Security Code</span>")
                  .append("<div class='alert-box'>We received a request to reset your password. Use the verification code below:</div>")
                  .append("<div style='background:#edf2f7; text-align:center; padding:18px; border-radius:8px; margin:16px 0;'><span style='font-family:monospace; font-size:32px; font-weight:800; letter-spacing:8px; color:#2b6cb0;'>").append(otpCode).append("</span></div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Email / Username:</td><td class='val'>").append(userOtpId).append("</td></tr>")
                  .append("<tr><td class='lbl'>OTP Expiry Time:</td><td class='val' style='color:#c53030;'>10 minutes</td></tr>")
                  .append("<tr><td class='lbl'>Request Date / Time:</td><td class='val'>").append(reqTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>Security Warning:</td><td class='val' style='color:#c53030;'>").append(secWarn).append("</td></tr>")
                  .append("</table>");
                break;

            case "RESCHEDULING":
                sb.append("<span class='badge badge-amber'>Flight Rescheduled</span>")
                  .append("<div class='alert-box alert-warn'>Your flight schedule has been updated. Please review your new departure details:</div>")
                  .append("<table class='info-table'>")
                  .append("<tr><td class='lbl'>Customer Name:</td><td class='val'>").append(customerName).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking ID / PNR:</td><td class='val val-highlight'>").append(bookingRef).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight ID:</td><td class='val'>").append(req.getFlightId() != null ? req.getFlightId() : 1).append("</td></tr>")
                  .append("<tr><td class='lbl'>Flight Number:</td><td class='val'>").append(flightNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Airline:</td><td class='val'>").append(airline).append("</td></tr>")
                  .append("<tr><td class='lbl'>From:</td><td class='val'>").append(origin).append("</td></tr>")
                  .append("<tr><td class='lbl'>To:</td><td class='val'>").append(dest).append("</td></tr>")
                  .append("<tr><td class='lbl'>New Departure Schedule:</td><td class='val val-highlight'>").append(depDate).append(" at ").append(depTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>New Arrival Schedule:</td><td class='val'>").append(arrDate).append(" at ").append(arrTime).append("</td></tr>")
                  .append("<tr><td class='lbl'>New Seat Number:</td><td class='val val-highlight'>").append(seatNum).append("</td></tr>")
                  .append("<tr><td class='lbl'>Passenger Count:</td><td class='val'>").append(paxCount).append("</td></tr>")
                  .append("<tr><td class='lbl'>Total Amount:</td><td class='val-amount'>").append(String.format("%.2f %s", totalAmt, currency)).append("</td></tr>")
                  .append("<tr><td class='lbl'>Rescheduled Date/Time:</td><td class='val'>").append(req.getRescheduledDateTime() != null ? req.getRescheduledDateTime() : now).append("</td></tr>")
                  .append("<tr><td class='lbl'>Booking Status:</td><td class='val-amount'>RESCHEDULED</td></tr>")
                  .append("</table>")
                  .append("<div class='pdf-bar'><strong>&#128196; Updated PDF Travel Document Attached</strong><br/>Your updated itinerary and tax document is attached.</div>");
                break;

            default:
                sb.append("<div class='alert-box'>").append(req.getMessage() != null ? req.getMessage() : "Operation completed.").append("</div>");
                break;
        }

        sb.append("</div>")
          .append("<div class='footer'>&copy; 2026 SkyWings Airlines System. All rights reserved.<br/>Customer Care: support@skywings.com | +91-1800-SKY-WINGS</div>")
          .append("</div></body></html>");

        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return mapToNotificationResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByBookingId(Long bookingId) {
        return notificationRepository.findByBookingId(bookingId).stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getBookingId(),
                notification.getNotificationType(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
