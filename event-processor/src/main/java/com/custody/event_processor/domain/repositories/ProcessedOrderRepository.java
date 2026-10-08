package com.custody.event_processor.domain.repositories;

import com.custody.event_processor.domain.models.ProcessedOrder;
import java.util.Optional;
import java.util.UUID;

public interface ProcessedOrderRepository {
    ProcessedOrder save(ProcessedOrder processedOrder);
    Optional<ProcessedOrder> findByOrderId(UUID orderId);
}
