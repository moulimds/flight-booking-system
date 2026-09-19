package com.flight.notification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class NotificationRequest {

    private Long userId;
    private Long bookingId;
    private String notificationType = "EMAIL"; // EMAIL, SMS, PUSH

    @JsonAlias({"recipientEmail", "email", "to"})
    private String recipient = "sindhusakthi41@gmail.com";

    private String subject;

    @JsonAlias({"content", "body", "text"})
    private String message;

    private String status = "SENT";
    private String bookingReference;
    private String paymentReference;
    private String invoiceNumber;
    private Long flightId;
    private String flightNumber;

    @JsonAlias({"airline", "airlineName"})
    private String flightName;

    @JsonAlias({"passengerName", "name", "fullName"})
    private String customerName;

    private String username;

    @JsonAlias({"source", "from", "departureCity"})
    private String origin;

    @JsonAlias({"dest", "toCity", "arrivalCity"})
    private String destination;

    private String departureDate;
    private String departureTime;
    private String arrivalDate;
    private String arrivalTime;

    @JsonAlias({"cabinClass", "class"})
    private String seatClass = "ECONOMY";

    private String seatNumber;
    private Double amount;
    private String currency = "INR";
    private String otp;
    private String reason;
    private Boolean attachPdf;

    // Registration specific fields
    private String registrationDateTime;
    private String accountStatus;
    @JsonAlias({"verificationUrl", "verifyLink"})
    private String verificationLink;
    private String otpExpiry;
    private String customerId;
    private String securityMessage;
    private String securityWarning;

    // Booking & Cancellation specific fields
    private String bookingStatus;
    private Integer passengerCount;
    private String travelDate;
    private String cancelledDateTime;
    private String cancellationReason;
    private Double cancellationFee;
    private Double refundAmount;
    private String cancellationStatus;

    // Payment & Invoicing specific fields
    private String paymentId;
    private String paymentMethod;
    private String transactionDateTime;
    private String paymentStatus;
    private String billingAddress;
    private String passengerDetails;
    private Double baseFare;
    private Double taxes;
    private Double convenienceFee;
    private Double otherCharges;
    private Double discount;
    private Double totalAmount;
    private String invoiceDate;

    // Refund specific fields
    private String refundId;
    private Double originalPaymentAmount;
    private String originalTransactionId;
    private String refundInitiatedDate;
    private String refundCompletionDate;
    private String refundStatus;
    private String expectedCreditInfo;
    private String expectedTimeframe;

    // Password & Security specific fields
    private String changeDateTime;
    private String deviceInfo;
    private String ipAddress;
    private String requestDateTime;
    private String rescheduledDateTime;

    public NotificationRequest() {
    }

    public NotificationRequest(Long userId, Long bookingId, String notificationType, String recipient,
                               String subject, String message, String status) {
        this.userId = userId;
        this.bookingId = bookingId;
        this.notificationType = notificationType != null ? notificationType : "EMAIL";
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.status = status != null ? status : "SENT";
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }

    public String getFlightName() { return flightName; }
    public void setFlightName(String flightName) { this.flightName = flightName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getAttachPdf() { return attachPdf; }
    public void setAttachPdf(Boolean attachPdf) { this.attachPdf = attachPdf; }

    public String getRegistrationDateTime() { return registrationDateTime; }
    public void setRegistrationDateTime(String registrationDateTime) { this.registrationDateTime = registrationDateTime; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getVerificationLink() { return verificationLink; }
    public void setVerificationLink(String verificationLink) { this.verificationLink = verificationLink; }

    public String getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(String otpExpiry) { this.otpExpiry = otpExpiry; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getSecurityMessage() { return securityMessage; }
    public void setSecurityMessage(String securityMessage) { this.securityMessage = securityMessage; }

    public String getSecurityWarning() { return securityWarning; }
    public void setSecurityWarning(String securityWarning) { this.securityWarning = securityWarning; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }

    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }

    public String getCancelledDateTime() { return cancelledDateTime; }
    public void setCancelledDateTime(String cancelledDateTime) { this.cancelledDateTime = cancelledDateTime; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Double getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(Double cancellationFee) { this.cancellationFee = cancellationFee; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public String getCancellationStatus() { return cancellationStatus; }
    public void setCancellationStatus(String cancellationStatus) { this.cancellationStatus = cancellationStatus; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionDateTime() { return transactionDateTime; }
    public void setTransactionDateTime(String transactionDateTime) { this.transactionDateTime = transactionDateTime; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getPassengerDetails() { return passengerDetails; }
    public void setPassengerDetails(String passengerDetails) { this.passengerDetails = passengerDetails; }

    public Double getBaseFare() { return baseFare; }
    public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

    public Double getTaxes() { return taxes; }
    public void setTaxes(Double taxes) { this.taxes = taxes; }

    public Double getConvenienceFee() { return convenienceFee; }
    public void setConvenienceFee(Double convenienceFee) { this.convenienceFee = convenienceFee; }

    public Double getOtherCharges() { return otherCharges; }
    public void setOtherCharges(Double otherCharges) { this.otherCharges = otherCharges; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public Double getOriginalPaymentAmount() { return originalPaymentAmount; }
    public void setOriginalPaymentAmount(Double originalPaymentAmount) { this.originalPaymentAmount = originalPaymentAmount; }

    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }

    public String getRefundInitiatedDate() { return refundInitiatedDate; }
    public void setRefundInitiatedDate(String refundInitiatedDate) { this.refundInitiatedDate = refundInitiatedDate; }

    public String getRefundCompletionDate() { return refundCompletionDate; }
    public void setRefundCompletionDate(String refundCompletionDate) { this.refundCompletionDate = refundCompletionDate; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }

    public String getExpectedCreditInfo() { return expectedCreditInfo; }
    public void setExpectedCreditInfo(String expectedCreditInfo) { this.expectedCreditInfo = expectedCreditInfo; }

    public String getExpectedTimeframe() { return expectedTimeframe; }
    public void setExpectedTimeframe(String expectedTimeframe) { this.expectedTimeframe = expectedTimeframe; }

    public String getChangeDateTime() { return changeDateTime; }
    public void setChangeDateTime(String changeDateTime) { this.changeDateTime = changeDateTime; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getRequestDateTime() { return requestDateTime; }
    public void setRequestDateTime(String requestDateTime) { this.requestDateTime = requestDateTime; }

    public String getRescheduledDateTime() { return rescheduledDateTime; }
    public void setRescheduledDateTime(String rescheduledDateTime) { this.rescheduledDateTime = rescheduledDateTime; }
}
