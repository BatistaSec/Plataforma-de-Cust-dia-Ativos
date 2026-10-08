package com.custody.event_processor.infrastructure.messaging.consumers;

import com.custody.event_processor.application.usecases.ProcessOrderEventUseCase;
import com.custody.event_processor.domain.events.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProcessOrderEventUseCase processOrderEventUseCase;

    public OrderEventConsumer(ProcessOrderEventUseCase processOrderEventUseCase) {
        this.processOrderEventUseCase = processOrderEventUseCase;
    }

    @KafkaListener(topics = "${kafka.topic.custody-orders:custody-orders-topic}", groupId = "${spring.kafka.consumer.group-id:event-processor-group}")
    public void consume(OrderEvent event) {
        log.info("Received OrderEvent from Kafka: {}", event.getOrderId());
        try {
            processOrderEventUseCase.execute(event);
        } catch (Exception e) {
            log.error("Error processing OrderEvent: {}", event.getOrderId(), e);
            // TODO: Implement dead letter queue or retry logic
        }
    }
}
