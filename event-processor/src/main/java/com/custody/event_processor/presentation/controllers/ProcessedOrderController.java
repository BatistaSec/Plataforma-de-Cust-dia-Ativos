package com.custody.event_processor.presentation.controllers;

import com.custody.event_processor.application.dto.ProcessedOrderOutput;
import com.custody.event_processor.application.usecases.GetProcessedOrderUseCase;
import com.custody.event_processor.application.usecases.ProcessOrderEventUseCase;
import com.custody.event_processor.domain.enums.OrderStatus;
import com.custody.event_processor.domain.events.OrderEvent;
import com.custody.event_processor.domain.exceptions.ResourceNotFoundException;
import com.custody.event_processor.presentation.request.ManualProcessOrderRequest;
import com.custody.event_processor.presentation.response.ProcessedOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processed-orders")
public class ProcessedOrderController {

    private final GetProcessedOrderUseCase getProcessedOrderUseCase;
    private final ProcessOrderEventUseCase processOrderEventUseCase;

    public ProcessedOrderController(GetProcessedOrderUseCase getProcessedOrderUseCase,
                                     ProcessOrderEventUseCase processOrderEventUseCase) {
        this.getProcessedOrderUseCase = getProcessedOrderUseCase;
        this.processOrderEventUseCase = processOrderEventUseCase;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ProcessedOrderResponse> getByOrderId(@PathVariable UUID orderId) {
        ProcessedOrderOutput output = getProcessedOrderUseCase.execute(orderId);
        return ResponseEntity.ok(toResponse(output));
    }

    /**
     * Endpoint para reprocessar manualmente uma ordem já existente no banco.
     * Útil para reprocessar ordens que ficaram presas em PENDING/PROCESSING.
     */
    @PostMapping("/reprocess")
    public ResponseEntity<ProcessedOrderResponse> reprocess(@Valid @RequestBody ManualProcessOrderRequest request) {
        // busca a ordem para obter os dados completos necessários para o evento
        ProcessedOrderOutput existing = getProcessedOrderUseCase.execute(request.getOrderId());

        OrderEvent event = new OrderEvent(
                existing.getOrderId(),
                existing.getPortfolioId(),
                existing.getAssetId(),
                existing.getType(),
                existing.getQuantity(),
                OrderStatus.PENDING
        );

        processOrderEventUseCase.execute(event);

        ProcessedOrderOutput output = getProcessedOrderUseCase.execute(request.getOrderId());
        return ResponseEntity.ok(toResponse(output));
    }

    private ProcessedOrderResponse toResponse(ProcessedOrderOutput output) {
        return new ProcessedOrderResponse(
                output.getId(),
                output.getOrderId(),
                output.getPortfolioId(),
                output.getAssetId(),
                output.getType(),
                output.getQuantity(),
                output.getStatus(),
                output.getProcessedAt()
        );
    }
}
