package com.airline.notification.service;

import com.airline.notification.exception.NotificationDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsSenderService {

    private static final Logger log = LoggerFactory.getLogger(SmsSenderService.class);

    public boolean sendSms(String phoneNumber, String message) {
        log.info("======================= [DISPATCHING SMS] =======================");
        log.info("TO: {}", phoneNumber);
        log.info("MESSAGE: {}", message);
        log.info("=================================================================");

        if (phoneNumber == null || phoneNumber.trim().length() < 7) {
            throw new NotificationDeliveryException("Invalid phone number: " + phoneNumber);
        }

        // Simulate SMS gateway provider dispatch
        return true;
    }
}
