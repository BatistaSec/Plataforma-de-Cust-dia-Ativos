package com.custody.audit_service.presentation.response;

import java.util.List;

/**
 * Envelope de paginação para respostas da API de auditoria.
 *
 * <p>Segue o padrão de paginação do Spring Data (page zero-indexed) mas sem
 * expor dependências do framework no contrato HTTP.
 */
public class PagedAuditResponse {

    private final List<AuditLogResponse> content;
    private final int     page;
    private final int     size;
    private final long    totalElements;
    private final int     totalPages;
    private final boolean first;
    private final boolean last;

    public PagedAuditResponse(
            List<AuditLogResponse> content,
            int  page,
            int  size,
            long totalElements,
            int  totalPages,
            boolean first,
            boolean last) {
        this.content       = content;
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.first         = first;
        this.last          = last;
    }

    public List<AuditLogResponse> getContent()       { return content; }
    public int     getPage()                         { return page; }
    public int     getSize()                         { return size; }
    public long    getTotalElements()                { return totalElements; }
    public int     getTotalPages()                   { return totalPages; }
    public boolean isFirst()                         { return first; }
    public boolean isLast()                          { return last; }
}
