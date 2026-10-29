package com.custody.audit_service.infrastructure.persistence.repositories;

import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.infrastructure.persistence.entities.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    List<AuditLogEntity> findByOrderId(UUID orderId);

    List<AuditLogEntity> findByPortfolioId(UUID portfolioId);

    /**
     * Busca filtrada e paginada por portfólio, sem filtro de eventType.
     * Resultado ordenado por occurredAt DESC (mais recente primeiro).
     */
    @Query("""
            SELECT e FROM AuditLogEntity e
            WHERE e.portfolioId = :portfolioId
              AND (:from IS NULL OR e.occurredAt >= :from)
              AND (:to   IS NULL OR e.occurredAt <= :to)
            ORDER BY e.occurredAt DESC
            """)
    Page<AuditLogEntity> findByPortfolioIdPaged(
            @Param("portfolioId") UUID portfolioId,
            @Param("from")        LocalDateTime from,
            @Param("to")          LocalDateTime to,
            Pageable pageable
    );

    /**
     * Busca filtrada e paginada por portfólio, com filtro de eventType.
     * Tipo explícito no binding evita ambiguidade do Hibernate com enums nullable.
     */
    @Query("""
            SELECT e FROM AuditLogEntity e
            WHERE e.portfolioId = :portfolioId
              AND e.eventType   = :eventType
              AND (:from IS NULL OR e.occurredAt >= :from)
              AND (:to   IS NULL OR e.occurredAt <= :to)
            ORDER BY e.occurredAt DESC
            """)
    Page<AuditLogEntity> findByPortfolioIdAndEventTypePaged(
            @Param("portfolioId") UUID portfolioId,
            @Param("eventType")   AuditEventType eventType,
            @Param("from")        LocalDateTime from,
            @Param("to")          LocalDateTime to,
            Pageable pageable
    );

    /**
     * Busca filtrada e paginada por orderId, sem filtro de eventType.
     */
    @Query("""
            SELECT e FROM AuditLogEntity e
            WHERE e.orderId = :orderId
              AND (:from IS NULL OR e.occurredAt >= :from)
              AND (:to   IS NULL OR e.occurredAt <= :to)
            ORDER BY e.occurredAt DESC
            """)
    Page<AuditLogEntity> findByOrderIdPaged(
            @Param("orderId") UUID orderId,
            @Param("from")    LocalDateTime from,
            @Param("to")      LocalDateTime to,
            Pageable pageable
    );

    /**
     * Busca filtrada e paginada por orderId, com filtro de eventType.
     */
    @Query("""
            SELECT e FROM AuditLogEntity e
            WHERE e.orderId   = :orderId
              AND e.eventType = :eventType
              AND (:from IS NULL OR e.occurredAt >= :from)
              AND (:to   IS NULL OR e.occurredAt <= :to)
            ORDER BY e.occurredAt DESC
            """)
    Page<AuditLogEntity> findByOrderIdAndEventTypePaged(
            @Param("orderId")   UUID orderId,
            @Param("eventType") AuditEventType eventType,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            Pageable pageable
    );
}
