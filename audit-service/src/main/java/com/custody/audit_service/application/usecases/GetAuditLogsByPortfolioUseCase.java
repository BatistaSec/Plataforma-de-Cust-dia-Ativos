package com.custody.audit_service.application.usecases;

import com.custody.audit_service.application.dto.AuditLogOutput;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetAuditLogsByPortfolioUseCase {

    private final AuditLogRepository auditLogRepository;

    public GetAuditLogsByPortfolioUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogOutput> execute(UUID portfolioId) {
        return auditLogRepository.findByPortfolioId(portfolioId)
                .stream()
                .map(this::toOutput)
                .collect(Collectors.toList());
    }

    private AuditLogOutput toOutput(AuditLog log) {
        return new AuditLogOutput(
                log.getId(),
                log.getOrderId(),
                log.getPortfolioId(),
                log.getAssetId(),
                log.getEventType(),
                log.getPayload(),
                log.getOccurredAt()
        );
    }
}
