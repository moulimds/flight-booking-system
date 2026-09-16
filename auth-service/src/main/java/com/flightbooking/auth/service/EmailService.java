package com.flightbooking.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendPasswordResetOtp(String toEmail, String otp) {
        String subject = "Flight Booking System - Password Reset OTP";
        String body = "Hello,\n\nYour OTP for resetting your password is: " + otp + 
                      "\n\nThis OTP is valid for 10 minutes. If you did not request a password reset, please ignore this email.\n\nBest regards,\nFlight Booking Team";
        sendEmail(toEmail, subject, body);
    }

    public void sendRegistrationVerificationOtp(String toEmail, String otp) {
        String subject = "Flight Booking System - Verify Your Email";
        String body = "Hello,\n\nWelcome to Flight Booking System! Your email verification code is: " + otp + 
                      "\n\nThis code is valid for 15 minutes.\n\nBest regards,\nFlight Booking Team";
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Email successfully sent to: {}", to);
            } else {
                log.warn("JavaMailSender is not configured. Simulating email send.");
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}. Falling back to console log.", to, e.getMessage());
        }
        
        System.out.println("==================================================");
        System.out.println(">>> [EMAIL SENT]");
        System.out.println(">>> TO: " + to);
        System.out.println(">>> SUBJECT: " + subject);
        System.out.println(">>> BODY:\n" + body);
        System.out.println("==================================================");
    }
}
