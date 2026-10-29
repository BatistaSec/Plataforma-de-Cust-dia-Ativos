package com.custody.audit_service.domain.query;

import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.domain.enums.FilterSubject;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Critério de busca para consulta paginada de registros de auditoria.
 *
 * <p>Pertence ao domínio pois encapsula regras de negócio sobre como uma
 * consulta de auditoria é formulada e quais valores são válidos.
 *
 * <p>Validações de negócio são aplicadas no construtor para garantir que
 * apenas critérios válidos cheguem à camada de infraestrutura.
 */
public final class AuditLogFilter {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_PAGE_SIZE = 1;

    private final UUID           subjectId;
    private final FilterSubject  subject;
    private final AuditEventType eventType;
    private final LocalDateTime  from;
    private final LocalDateTime  to;
    private final int            page;
    private final int            size;

    public AuditLogFilter(
            UUID subjectId,
            FilterSubject subject,
            AuditEventType eventType,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException("O número de página não pode ser negativo");
        }
        if (size < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("O tamanho mínimo de página é " + MIN_PAGE_SIZE);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("O tamanho máximo de página é " + MAX_PAGE_SIZE);
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("O parâmetro 'from' deve ser anterior ao 'to'");
        }

        this.subjectId = subjectId;
        this.subject   = subject;
        this.eventType = eventType;
        this.from      = from;
        this.to        = to;
        this.page      = page;
        this.size      = size;
    }

    public UUID           getSubjectId() { return subjectId; }
    public FilterSubject  getSubject()   { return subject; }
    public AuditEventType getEventType() { return eventType; }
    public LocalDateTime  getFrom()      { return from; }
    public LocalDateTime  getTo()        { return to; }
    public int            getPage()      { return page; }
    public int            getSize()      { return size; }
}
