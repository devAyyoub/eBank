package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.dto.TransactionEvent;
import com.ebanking.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "transaction-events", groupId = "notification-service-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("=== KAFKA CONSUMER: Received event ===");
        log.info("Event details: transactionId={}, type={}, amount={}, status={}, userId={}",
                event != null ? event.getTransactionId() : "null",
                event != null ? event.getTransactionType() : "null",
                event != null ? event.getAmount() : "null",
                event != null ? event.getStatus() : "null",
                event != null ? event.getUserId() : "null");

        if (event == null) {
            log.error("Received null event!");
            return;
        }

        try {
            if ("COMPLETED".equals(event.getStatus())) {
                log.info("Processing COMPLETED transaction event: transactionId={}", event.getTransactionId());
                createNotificationForTransaction(event);
            } else {
                log.info(
                        "Skipping transaction event with status: {} (only COMPLETED transactions create notifications)",
                        event.getStatus());
            }
        } catch (Exception e) {
            log.error("ERROR processing transaction event: transactionId={}, error={}, stackTrace:",
                    event.getTransactionId(), e.getMessage(), e);
        }
    }

    private void createNotificationForTransaction(TransactionEvent event) {
        log.info("=== Creating notification ===");
        log.info("TransactionId: {}, UserId: {}, Status: {}",
                event.getTransactionId(), event.getUserId(), event.getStatus());

        if (event.getUserId() == null) {
            log.error("Cannot create notification: userId is null for transaction: {}", event.getTransactionId());
            return;
        }

        if (event.getTransactionId() == null || event.getTransactionId().isEmpty()) {
            log.error("Cannot create notification: transactionId is null or empty");
            return;
        }

        try {
            Notification notification = new Notification();
            notification.setUserId(event.getUserId());
            notification.setType(Notification.NotificationType.EMAIL);
            notification.setNotificationType(Notification.NotificationType.EMAIL);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setNotificationId("NOTIF-" + event.getTransactionId());

            notification.setRecipient("user-" + event.getUserId() + "@ebank.local");

            String message = buildNotificationMessage(event);
            if (message == null || message.isEmpty()) {
                message = "Transaction " + event.getTransactionType() + " completed";
            }
            notification.setMessage(message);

            String subject = "Transaction "
                    + (event.getTransactionType() != null ? event.getTransactionType() : "Unknown");
            notification.setSubject(subject);

            log.info("Prepared notification - userId: {}, notificationId: {}, message length: {}",
                    notification.getUserId(), notification.getNotificationId(),
                    notification.getMessage() != null ? notification.getMessage().length() : 0);

            log.info("Calling notificationService.createNotification()...");
            Notification savedNotification = notificationService.createNotification(notification);

            if (savedNotification != null && savedNotification.getId() != null) {
                log.info("=== Notification saved successfully ===");
                log.info("Saved notification - id: {}, notificationId: {}, userId: {}, message: {}",
                        savedNotification.getId(),
                        savedNotification.getNotificationId(),
                        savedNotification.getUserId(),
                        savedNotification.getMessage());
            } else {
                log.error("=== Notification service returned null or notification without ID ===");
            }
        } catch (Exception e) {
            log.error("=== ERROR creating notification ===");
            log.error("TransactionId: {}, UserId: {}, Error: {}",
                    event.getTransactionId(), event.getUserId(), e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Stack trace:", e);
        }
    }

    private String buildNotificationMessage(TransactionEvent event) {
        try {
            StringBuilder message = new StringBuilder();
            message.append("Transaction ");
            message.append(event.getTransactionType() != null ? event.getTransactionType() : "Unknown");
            message.append(" completed. ");

            if (event.getAmount() != null) {
                message.append("Amount: ").append(event.getAmount());
                if (event.getCurrency() != null && !event.getCurrency().isEmpty()) {
                    message.append(" ").append(event.getCurrency());
                }
                message.append(". ");
            }

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                message.append("Description: ").append(event.getDescription()).append(". ");
            }

            message.append("Transaction ID: ").append(event.getTransactionId());

            return message.toString();
        } catch (Exception e) {
            log.error("Error building notification message: {}", e.getMessage());
            return "Transaction completed. Transaction ID: " + event.getTransactionId();
        }
    }
}
