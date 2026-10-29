package com.custody.event_processor.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ManualProcessOrderRequest {

    @NotNull(message = "Order ID é obrigatório")
    private UUID orderId;

    public ManualProcessOrderRequest() {}

    public ManualProcessOrderRequest(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}
