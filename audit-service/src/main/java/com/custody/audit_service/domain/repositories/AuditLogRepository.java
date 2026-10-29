package com.custody.audit_service.domain.repositories;

import com.custody.audit_service.domain.query.AuditLogFilter;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.models.Page;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByOrderId(UUID orderId);

    List<AuditLog> findByPortfolioId(UUID portfolioId);

    /**
     * Busca paginada e filtrada de registros de auditoria.
     * Suporta filtro opcional por portfólio, ordem, tipo de evento e janela temporal.
     *
     * @param filter critérios de busca e paginação
     * @return página de domínio (sem dependência de Spring Data)
     */
    Page<AuditLog> findPaged(AuditLogFilter filter);
}
