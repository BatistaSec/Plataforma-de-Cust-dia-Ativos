package com.custody.audit_service.infrastructure.persistence.adapters;

import com.custody.audit_service.domain.enums.FilterSubject;
import com.custody.audit_service.domain.models.AuditLog;
import com.custody.audit_service.domain.models.Page;
import com.custody.audit_service.domain.query.AuditLogFilter;
import com.custody.audit_service.domain.repositories.AuditLogRepository;
import com.custody.audit_service.infrastructure.mappers.AuditLogMapper;
import com.custody.audit_service.infrastructure.persistence.entities.AuditLogEntity;
import com.custody.audit_service.infrastructure.persistence.repositories.SpringDataAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final SpringDataAuditLogRepository springDataRepository;

    public AuditLogRepositoryImpl(SpringDataAuditLogRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        var entity = AuditLogMapper.toEntity(auditLog);
        var saved  = springDataRepository.save(entity);
        return AuditLogMapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId)
                .stream()
                .map(AuditLogMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByPortfolioId(UUID portfolioId) {
        return springDataRepository.findByPortfolioId(portfolioId)
                .stream()
                .map(AuditLogMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Implementa a busca paginada delegando para queries JPQL especializadas.
     *
     * <p>Usa métodos distintos com/sem eventType para evitar problemas de inferência
     * de tipo do Hibernate com parâmetros enum nullable em JPQL.
     * Converte o {@link org.springframework.data.domain.Page} do Spring para o
     * {@link Page} de domínio sem expor Spring Data à camada de domínio.
     */
    @Override
    public Page<AuditLog> findPaged(AuditLogFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        org.springframework.data.domain.Page<AuditLogEntity> springPage;

        if (filter.getSubject() == FilterSubject.PORTFOLIO) {
            if (filter.getEventType() != null) {
                springPage = springDataRepository.findByPortfolioIdAndEventTypePaged(
                        filter.getSubjectId(),
                        filter.getEventType(),
                        filter.getFrom(),
                        filter.getTo(),
                        pageable);
            } else {
                springPage = springDataRepository.findByPortfolioIdPaged(
                        filter.getSubjectId(),
                        filter.getFrom(),
                        filter.getTo(),
                        pageable);
            }
        } else {
            if (filter.getEventType() != null) {
                springPage = springDataRepository.findByOrderIdAndEventTypePaged(
                        filter.getSubjectId(),
                        filter.getEventType(),
                        filter.getFrom(),
                        filter.getTo(),
                        pageable);
            } else {
                springPage = springDataRepository.findByOrderIdPaged(
                        filter.getSubjectId(),
                        filter.getFrom(),
                        filter.getTo(),
                        pageable);
            }
        }

        List<AuditLog> content = springPage.getContent().stream()
                .map(AuditLogMapper::toDomain)
                .collect(Collectors.toList());

        return new Page<AuditLog>(
                content,
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }
}
