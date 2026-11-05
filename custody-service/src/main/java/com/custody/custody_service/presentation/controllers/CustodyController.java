package com.custody.custody_service.presentation.controllers;

import com.custody.custody_service.application.dto.*;
import com.custody.custody_service.application.usecases.*;
import com.custody.custody_service.presentation.request.CreateAssetRequest;
import com.custody.custody_service.presentation.request.CreatePortfolioRequest;
import com.custody.custody_service.presentation.request.CustodyOrderRequest;
import com.custody.custody_service.presentation.request.GetPortfolioRequest;
import com.custody.custody_service.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/custody")
public class CustodyController {

    private final CreatePortfolioUseCase createPortfolioUseCase;
    private final GetPortfolioUseCase getPortfolioUseCase;
    private final CreateAssetUseCase createAssetUseCase;
    private final GetAllAssetsUseCase getAllAssetsUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final GetLedgerStatementUseCase getLedgerStatementUseCase;

    public CustodyController(CreatePortfolioUseCase createPortfolioUseCase,
                              GetPortfolioUseCase getPortfolioUseCase,
                              CreateAssetUseCase createAssetUseCase,
                              GetAllAssetsUseCase getAllAssetsUseCase,
                              CreateOrderUseCase createOrderUseCase,
                              GetLedgerStatementUseCase getLedgerStatementUseCase) {
        this.createPortfolioUseCase = createPortfolioUseCase;
        this.getPortfolioUseCase = getPortfolioUseCase;
        this.createAssetUseCase = createAssetUseCase;
        this.getAllAssetsUseCase = getAllAssetsUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.getLedgerStatementUseCase = getLedgerStatementUseCase;
    }

    @PostMapping("/portfolios")
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        var input = new CreatePortfolioInput(request.getUserId());
        var output = createPortfolioUseCase.execute(input);
        return ResponseEntity.ok(toPortfolioResponse(output));
    }

    @PostMapping("/portfolios/user")
    public ResponseEntity<PortfolioResponse> getPortfolio(@Valid @RequestBody GetPortfolioRequest request) {
        var output = getPortfolioUseCase.execute(request.getUserId());
        return ResponseEntity.ok(toPortfolioResponse(output));
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        var output = createAssetUseCase.execute(request.getTicker(), request.getName());
        return ResponseEntity.ok(new AssetResponse(output.getId(), output.getTicker(), output.getName()));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        var outputs = getAllAssetsUseCase.execute();
        var responses = outputs.stream()
                .map(o -> new AssetResponse(o.getId(), o.getTicker(), o.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/orders")
    public ResponseEntity<CustodyOrderResponse> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody CustodyOrderRequest request) {
        var input = new CreateOrderInput(
                request.getPortfolioId(),
                request.getAssetId(),
                request.getType(),
                request.getQuantity()
        );
        var output = createOrderUseCase.execute(input, idempotencyKey);
        var response = new CustodyOrderResponse(
                output.getId(),
                output.getPortfolioId(),
                output.getAssetId(),
                output.getType(),
                output.getQuantity(),
                output.getStatus(),
                output.getCreatedAt(),
                output.getIdempotencyKey()
        );
        ResponseEntity.BodyBuilder builder = output.isAlreadyExisted()
                ? ResponseEntity.ok()
                : ResponseEntity.status(HttpStatus.CREATED);
        return builder
                .header("Idempotency-Key", output.getIdempotencyKey().toString())
                .body(response);
    }

    @GetMapping("/ledger/statement")
    public ResponseEntity<LedgerStatementResponse> getLedgerStatement(
            @RequestParam UUID portfolioId,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var output = getLedgerStatementUseCase.execute(portfolioId, assetId, page, size);
        var entries = output.getEntries().stream()
                .map(e -> new LedgerEntryResponse(
                        e.getId(), e.getTransactionId(), e.getAccountId(), e.getAssetId(),
                        e.getEntryType(), e.getQuantity(), e.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new LedgerStatementResponse(
                entries, output.getPage(), output.getSize(),
                output.getTotalElements(), output.getTotalPages()));
    }

    private PortfolioResponse toPortfolioResponse(PortfolioOutput output) {
        var assetResponses = output.getAssets().stream()
                .map(a -> new PortfolioAssetResponse(a.getId(), a.getAssetId(), a.getTicker(), a.getAssetName(), a.getQuantity()))
                .collect(Collectors.toList());
        return new PortfolioResponse(output.getId(), output.getUserId(), assetResponses);
    }
}
