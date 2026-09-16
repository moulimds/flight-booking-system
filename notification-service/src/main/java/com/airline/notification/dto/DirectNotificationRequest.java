package com.airline.notification.dto;

import com.airline.notification.entity.NotificationChannel;
import com.airline.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class DirectNotificationRequest {

    private String userId;

    @NotBlank(message = "Recipient is required (email address or phone number)")
    private String recipient;

    @NotNull(message = "Channel is required (EMAIL or SMS)")
    private NotificationChannel channel;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private String subject;

    private String content;

    private String templateCode;

    private Map<String, String> templateVariables;

    public DirectNotificationRequest() {
    }

    public DirectNotificationRequest(String userId, String recipient, NotificationChannel channel,
                                     NotificationType type, String subject, String content) {
        this.userId = userId;
        this.recipient = recipient;
        this.channel = channel;
        this.type = type;
        this.subject = subject;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, String> getTemplateVariables() {
        return templateVariables;
    }

    public void setTemplateVariables(Map<String, String> templateVariables) {
        this.templateVariables = templateVariables;
    }
}
