package com.custody.audit_service.domain.models;

import java.util.List;

/**
 * Wrapper de paginação do domínio — sem dependência de Spring Data.
 * Transporta resultados paginados entre camada de infraestrutura e aplicação.
 *
 * @param <T> tipo do item de conteúdo
 */
public record Page<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements
) {
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return totalPages() == 0 || page >= totalPages() - 1;
    }
}
