package com.airline.notification.dto;

import com.airline.notification.entity.NotificationChannel;
import com.airline.notification.entity.NotificationLog;
import com.airline.notification.entity.NotificationStatus;
import com.airline.notification.entity.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String userId;
    private String recipient;
    private NotificationChannel channel;
    private NotificationType type;
    private String subject;
    private String content;
    private NotificationStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse from(NotificationLog log) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setRecipient(log.getRecipient());
        dto.setChannel(log.getChannel());
        dto.setType(log.getType());
        dto.setSubject(log.getSubject());
        dto.setContent(log.getContent());
        dto.setStatus(log.getStatus());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
