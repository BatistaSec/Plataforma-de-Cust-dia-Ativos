package com.custody.event_processor.presentation.request;

import java.util.UUID;

public class ManualProcessOrderRequest {
    private UUID orderId;

    public ManualProcessOrderRequest() {}

    public ManualProcessOrderRequest(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
