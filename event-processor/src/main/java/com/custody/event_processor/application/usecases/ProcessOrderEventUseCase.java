package com.custody.event_processor.application.usecases;

import com.custody.event_processor.domain.enums.OrderStatus;
import com.custody.event_processor.domain.events.OrderEvent;
import com.custody.event_processor.domain.events.ProcessedOrderEvent;
import com.custody.event_processor.domain.models.ProcessedOrder;
import com.custody.event_processor.domain.repositories.ProcessedOrderRepository;
import com.custody.event_processor.infrastructure.messaging.producers.ProcessedOrderProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProcessOrderEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessOrderEventUseCase.class);

    private final ProcessedOrderRepository processedOrderRepository;
    private final ProcessedOrderProducer processedOrderProducer;

    public ProcessOrderEventUseCase(ProcessedOrderRepository processedOrderRepository, ProcessedOrderProducer processedOrderProducer) {
        this.processedOrderRepository = processedOrderRepository;
        this.processedOrderProducer = processedOrderProducer;
    }

    public void execute(OrderEvent event) {
        log.info("Processando OrderEvent para o ID do Pedido: {}, Status: {}", event.getOrderId(), event.getStatus());
        

        if (processedOrderRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("ID do Pedido: {} já processado. Pulando.", event.getOrderId());
            return;
        }


        OrderStatus newStatus = OrderStatus.COMPLETED;

        ProcessedOrder processedOrder = new ProcessedOrder(
                null,
                event.getOrderId(),
                event.getPortfolioId(),
                event.getAssetId(),
                event.getType(),
                event.getQuantity(),
                newStatus,
                LocalDateTime.now()
        );

        processedOrderRepository.save(processedOrder);
        

        ProcessedOrderEvent processedEvent = new ProcessedOrderEvent(
                event.getOrderId(),
                newStatus,
                LocalDateTime.now()
        );
        processedOrderProducer.send(processedEvent);
        
        log.info("ID do Pedido: {} Processado e salvo com sucesso.", event.getOrderId());
    }
}
