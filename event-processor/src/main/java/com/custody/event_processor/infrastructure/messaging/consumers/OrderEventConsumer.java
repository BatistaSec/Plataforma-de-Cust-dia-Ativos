package com.custody.event_processor.infrastructure.messaging.consumers;

import com.custody.event_processor.application.usecases.ProcessOrderEventUseCase;
import com.custody.event_processor.domain.events.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProcessOrderEventUseCase processOrderEventUseCase;

    public OrderEventConsumer(ProcessOrderEventUseCase processOrderEventUseCase) {
        this.processOrderEventUseCase = processOrderEventUseCase;
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "${kafka.topic.custody-orders:custody-orders-topic}", groupId = "${spring.kafka.consumer.group-id:event-processor-group}")
    public void consume(OrderEvent event) {
        log.info("Recebi um OrderEvent do Kafka: {}", event.getOrderId());
        processOrderEventUseCase.execute(event);
    }

    @DltHandler
    public void handleDlt(OrderEvent event) {
        log.error("Evento enviado para DLT após todas as tentativas. OrderId: {}", event.getOrderId());
    }
}
