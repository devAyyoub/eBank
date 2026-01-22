package com.ebanking.transactionservice.service;

import com.ebanking.transactionservice.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    
    private static final String TOPIC = "transaction-events";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    
    public void sendTransactionEvent(TransactionEvent event) {
        log.info("=== KafkaProducerService: Sending event to topic: {} ===", TOPIC);
        log.info("Event details: transactionId={}, userId={}, status={}, type={}, amount={}", 
            event.getTransactionId(), event.getUserId(), event.getStatus(), 
            event.getTransactionType(), event.getAmount());
        
        try {
            CompletableFuture<SendResult<String, TransactionEvent>> future = 
                kafkaTemplate.send(TOPIC, event.getTransactionId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("=== Transaction event sent successfully ===");
                    log.info("TransactionId: {}, Topic: {}, Partition: {}, Offset: {}", 
                        event.getTransactionId(), 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("=== Failed to send transaction event ===");
                    log.error("TransactionId: {}, Error: {}", event.getTransactionId(), ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            log.error("=== Exception sending transaction event ===");
            log.error("TransactionId: {}, Error: {}", event.getTransactionId(), e.getMessage(), e);
        }
    }
}
