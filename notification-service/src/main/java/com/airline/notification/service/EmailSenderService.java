package com.airline.notification.service;

import com.airline.notification.exception.NotificationDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    public boolean sendEmail(String toEmail, String subject, String body) {
        log.info("======================= [DISPATCHING EMAIL] =======================");
        log.info("TO: {}", toEmail);
        log.info("SUBJECT: {}", subject);
        log.info("BODY:\n{}", body);
        log.info("===================================================================");

        if (toEmail == null || !toEmail.contains("@")) {
            throw new NotificationDeliveryException("Invalid email recipient address: " + toEmail);
        }

        // Simulate successful SMTP gateway dispatch
        return true;
    }
}
