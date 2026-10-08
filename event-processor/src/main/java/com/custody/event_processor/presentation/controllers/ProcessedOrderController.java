package com.custody.event_processor.presentation.controllers;

import com.custody.event_processor.application.dto.ProcessedOrderOutput;
import com.custody.event_processor.application.usecases.GetProcessedOrderUseCase;
import com.custody.event_processor.presentation.response.ProcessedOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processed-orders")
public class ProcessedOrderController {

    private final GetProcessedOrderUseCase getProcessedOrderUseCase;

    public ProcessedOrderController(GetProcessedOrderUseCase getProcessedOrderUseCase) {
        this.getProcessedOrderUseCase = getProcessedOrderUseCase;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ProcessedOrderResponse> getByOrderId(@PathVariable UUID orderId) {
        ProcessedOrderOutput output = getProcessedOrderUseCase.execute(orderId);
        
        ProcessedOrderResponse response = new ProcessedOrderResponse(
                output.getId(),
                output.getOrderId(),
                output.getType(),
                output.getQuantity(),
                output.getStatus(),
                output.getProcessedAt()
        );
        
        return ResponseEntity.ok(response);
    }
}
