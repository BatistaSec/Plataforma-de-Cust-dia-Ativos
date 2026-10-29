package com.custody.audit_service.application.usecases;

import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.domain.enums.OrderStatus;
import com.custody.audit_service.domain.events.OrderProcessedEvent;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.repositories.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RecordOrderProcessedUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordOrderProcessedUseCase.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public RecordOrderProcessedUseCase(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void execute(OrderProcessedEvent event) {
        // determina o tipo de evento de auditoria baseado no status final
        AuditEventType eventType = event.getFinalStatus() == OrderStatus.FAILED
                ? AuditEventType.ORDER_FAILED
                : AuditEventType.ORDER_PROCESSED;

        log.info("Registrando auditoria {} para orderId: {}", eventType, event.getOrderId());

        String payload = toJson(event);

        AuditLog auditLog = new AuditLog(
                null,
                event.getOrderId(),
                null,   // processed-orders-topic não carrega portfolioId
                null,   // processed-orders-topic não carrega assetId
                eventType,
                payload,
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
