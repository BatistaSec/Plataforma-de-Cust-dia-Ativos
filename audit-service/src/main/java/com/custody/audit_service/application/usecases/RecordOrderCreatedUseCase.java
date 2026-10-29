package com.custody.audit_service.application.usecases;

import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.domain.events.OrderCreatedEvent;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.repositories.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RecordOrderCreatedUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordOrderCreatedUseCase.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public RecordOrderCreatedUseCase(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void execute(OrderCreatedEvent event) {
        log.info("Registrando auditoria ORDER_CREATED para orderId: {}", event.getOrderId());

        String payload = toJson(event);

        AuditLog log = new AuditLog(
                null,
                event.getOrderId(),
                event.getPortfolioId(),
                event.getAssetId(),
                AuditEventType.ORDER_CREATED,
                payload,
                LocalDateTime.now()
        );

        auditLogRepository.save(log);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
