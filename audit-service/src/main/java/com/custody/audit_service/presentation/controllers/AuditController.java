package com.custody.audit_service.presentation.controllers;

import com.custody.audit_service.application.dto.AuditLogOutput;
import com.custody.audit_service.application.usecases.GetPagedAuditLogsByOrderUseCase;
import com.custody.audit_service.application.usecases.GetPagedAuditLogsByPortfolioUseCase;
import com.custody.audit_service.domain.enums.AuditEventType;
import com.custody.audit_service.domain.models.Page;
import com.custody.audit_service.presentation.response.AuditLogResponse;
import com.custody.audit_service.presentation.response.PagedAuditResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final GetPagedAuditLogsByPortfolioUseCase getPagedByPortfolioUseCase;
    private final GetPagedAuditLogsByOrderUseCase     getPagedByOrderUseCase;

    public AuditController(
            GetPagedAuditLogsByPortfolioUseCase getPagedByPortfolioUseCase,
            GetPagedAuditLogsByOrderUseCase getPagedByOrderUseCase) {
        this.getPagedByPortfolioUseCase = getPagedByPortfolioUseCase;
        this.getPagedByOrderUseCase     = getPagedByOrderUseCase;
    }

    /**
     * Retorna os eventos de auditoria de uma ordem, paginados e com filtros opcionais.
     *
     * <p>Chamado sem parâmetros retorna a primeira página com 20 itens.
     *
     * <p>Exemplos:
     * <ul>
     *   <li>GET /api/v1/audit/order/{orderId}</li>
     *   <li>GET /api/v1/audit/order/{orderId}?page=0&amp;size=10&amp;eventType=ORDER_FAILED</li>
     *   <li>GET /api/v1/audit/order/{orderId}?from=2026-10-01T00:00:00&amp;to=2026-10-31T23:59:59</li>
     * </ul>
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PagedAuditResponse> getByOrder(
            @PathVariable UUID orderId,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLogOutput> domainPage =
                getPagedByOrderUseCase.execute(orderId, eventType, from, to, page, size);

        return ResponseEntity.ok(toPagedResponse(domainPage));
    }

    /**
     * Retorna os eventos de auditoria de um portfólio, paginados e com filtros opcionais.
     *
     * <p>Exemplos:
     * <ul>
     *   <li>GET /api/v1/audit/portfolio/{portfolioId}</li>
     *   <li>GET /api/v1/audit/portfolio/{portfolioId}?page=0&amp;size=10&amp;eventType=ORDER_CREATED</li>
     *   <li>GET /api/v1/audit/portfolio/{portfolioId}?from=2026-10-01T00:00:00&amp;size=50</li>
     * </ul>
     */
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<PagedAuditResponse> getByPortfolio(
            @PathVariable UUID portfolioId,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLogOutput> domainPage =
                getPagedByPortfolioUseCase.execute(portfolioId, eventType, from, to, page, size);

        return ResponseEntity.ok(toPagedResponse(domainPage));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PagedAuditResponse toPagedResponse(Page<AuditLogOutput> domainPage) {
        List<AuditLogResponse> items = domainPage.content().stream()
                .map(o -> new AuditLogResponse(
                        o.getId(),
                        o.getOrderId(),
                        o.getPortfolioId(),
                        o.getAssetId(),
                        o.getEventType(),
                        o.getPayload(),
                        o.getOccurredAt()))
                .collect(Collectors.toList());

        return new PagedAuditResponse(
                items,
                domainPage.page(),
                domainPage.size(),
                domainPage.totalElements(),
                domainPage.totalPages(),
                domainPage.isFirst(),
                domainPage.isLast()
        );
    }
}
