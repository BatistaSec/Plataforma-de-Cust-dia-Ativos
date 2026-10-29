package com.custody.audit_service.infrastructure.messaging.consumers;

import com.custody.audit_service.application.usecases.RecordOrderCreatedUseCase;
import com.custody.audit_service.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final RecordOrderCreatedUseCase recordOrderCreatedUseCase;

    public OrderCreatedConsumer(RecordOrderCreatedUseCase recordOrderCreatedUseCase) {
        this.recordOrderCreatedUseCase = recordOrderCreatedUseCase;
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = "-audit-dlt"
    )
    @KafkaListener(
        topics = "${kafka.topics.custody-orders:custody-orders-topic}",
        groupId = "${spring.kafka.consumer.group-id:audit-service-group}"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Auditoria: ORDER_CREATED recebido para orderId: {}", event.getOrderId());
        recordOrderCreatedUseCase.execute(event);
    }

    @DltHandler
    public void handleDlt(OrderCreatedEvent event) {
        log.error("Auditoria DLT: falha ao registrar ORDER_CREATED para orderId: {}", event.getOrderId());
    }
}
