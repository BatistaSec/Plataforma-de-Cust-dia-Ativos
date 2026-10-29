package com.custody.audit_service.infrastructure.messaging.consumers;

import com.custody.audit_service.application.usecases.RecordOrderProcessedUseCase;
import com.custody.audit_service.domain.events.OrderProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessedConsumer.class);

    private final RecordOrderProcessedUseCase recordOrderProcessedUseCase;

    public OrderProcessedConsumer(RecordOrderProcessedUseCase recordOrderProcessedUseCase) {
        this.recordOrderProcessedUseCase = recordOrderProcessedUseCase;
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = "-audit-dlt"
    )
    @KafkaListener(
        topics = "${kafka.topics.processed-orders:processed-orders-topic}",
        groupId = "${spring.kafka.consumer.group-id:audit-service-group}"
    )
    public void consume(OrderProcessedEvent event) {
        log.info("Auditoria: ORDER_PROCESSED recebido para orderId: {}", event.getOrderId());
        recordOrderProcessedUseCase.execute(event);
    }

    @DltHandler
    public void handleDlt(OrderProcessedEvent event) {
        log.error("Auditoria DLT: falha ao registrar ORDER_PROCESSED para orderId: {}", event.getOrderId());
    }
}
