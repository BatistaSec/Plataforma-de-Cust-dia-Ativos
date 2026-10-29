package com.custody.audit_service.application.usecases;

import com.custody.audit_service.domain.query.AuditLogFilter;
import com.custody.audit_service.application.dto.AuditLogOutput;
import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.domain.enums.FilterSubject;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.models.Page;
import com.custody.audit_service.domain.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: busca paginada e filtrada de registros de auditoria por ordem.
 *
 * <p>Espelha {@link GetPagedAuditLogsByPortfolioUseCase} com {@link FilterSubject#ORDER}.
 */
@Service
public class GetPagedAuditLogsByOrderUseCase {

    private final AuditLogRepository auditLogRepository;

    public GetPagedAuditLogsByOrderUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * @param orderId   ID da ordem a consultar
     * @param eventType filtro por tipo de evento (null = sem filtro)
     * @param from      início da janela temporal (null = sem limite inferior)
     * @param to        fim da janela temporal (null = sem limite superior)
     * @param page      número da página (zero-indexed)
     * @param size      itens por página (máx. 100)
     * @return página de registros de auditoria
     * @throws IllegalArgumentException se os parâmetros de paginação ou intervalo forem inválidos
     */
    public Page<AuditLogOutput> execute(
            UUID orderId,
            AuditEventType eventType,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        AuditLogFilter filter = new AuditLogFilter(
                orderId, FilterSubject.ORDER, eventType, from, to, page, size);

        Page<AuditLog> domainPage = auditLogRepository.findPaged(filter);

        return new Page<>(
                domainPage.content().stream().map(this::toOutput).toList(),
                domainPage.page(),
                domainPage.size(),
                domainPage.totalElements()
        );
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
