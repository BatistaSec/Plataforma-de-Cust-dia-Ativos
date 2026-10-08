package com.custody.event_processor.infrastructure.messaging.producers;

import com.custody.event_processor.domain.events.ProcessedOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcessedOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(ProcessedOrderProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topic.processed-orders:processed-orders-topic}")
    private String topic;

    public ProcessedOrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ProcessedOrderEvent event) {
        log.info("Publishing ProcessedOrderEvent for Order ID: {} to topic {}", event.getOrderId(), topic);
        kafkaTemplate.send(topic, event.getOrderId().toString(), event);
    }
}
