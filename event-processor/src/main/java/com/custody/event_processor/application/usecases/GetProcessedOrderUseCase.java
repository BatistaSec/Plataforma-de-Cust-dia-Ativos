package com.custody.event_processor.application.usecases;

import com.custody.event_processor.application.dto.ProcessedOrderOutput;
import com.custody.event_processor.domain.exceptions.ResourceNotFoundException;
import com.custody.event_processor.domain.models.ProcessedOrder;
import com.custody.event_processor.domain.repositories.ProcessedOrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetProcessedOrderUseCase {

    private final ProcessedOrderRepository repository;

    public GetProcessedOrderUseCase(ProcessedOrderRepository repository) {
        this.repository = repository;
    }

    public ProcessedOrderOutput execute(UUID orderId) {
        ProcessedOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido processado não encontrado para o ID do pedido: " + orderId));

        return new ProcessedOrderOutput(
                order.getId(),
                order.getOrderId(),
                order.getPortfolioId(),
                order.getAssetId(),
                order.getType(),
                order.getQuantity(),
                order.getStatus(),
                order.getProcessedAt()
        );
    }
}
