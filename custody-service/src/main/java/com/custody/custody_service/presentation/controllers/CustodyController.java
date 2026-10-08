package com.custody.custody_service.presentation.controllers;

import com.custody.custody_service.application.dto.*;
import com.custody.custody_service.application.usecases.*;
import com.custody.custody_service.presentation.request.CustodyOrderRequest;
import com.custody.custody_service.presentation.response.*;
import jakarta.validation.Valid;
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

    public CustodyController(CreatePortfolioUseCase createPortfolioUseCase,
                              GetPortfolioUseCase getPortfolioUseCase,
                              CreateAssetUseCase createAssetUseCase,
                              GetAllAssetsUseCase getAllAssetsUseCase,
                              CreateOrderUseCase createOrderUseCase) {
        this.createPortfolioUseCase = createPortfolioUseCase;
        this.getPortfolioUseCase = getPortfolioUseCase;
        this.createAssetUseCase = createAssetUseCase;
        this.getAllAssetsUseCase = getAllAssetsUseCase;
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping("/portfolios")
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestParam UUID userId) {
        var input = new CreatePortfolioInput(userId);
        var output = createPortfolioUseCase.execute(input);
        return ResponseEntity.ok(toPortfolioResponse(output));
    }

    @GetMapping("/portfolios/user/{userId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID userId) {
        var output = getPortfolioUseCase.execute(userId);
        return ResponseEntity.ok(toPortfolioResponse(output));
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponse> createAsset(@RequestParam String ticker, @RequestParam String name) {
        var output = createAssetUseCase.execute(ticker, name);
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
    public ResponseEntity<CustodyOrderResponse> createOrder(@Valid @RequestBody CustodyOrderRequest request) {
        var input = new CreateOrderInput(
                request.getPortfolioId(),
                request.getAssetId(),
                request.getType(),
                request.getQuantity()
        );
        var output = createOrderUseCase.execute(input);
        return ResponseEntity.ok(new CustodyOrderResponse(
                output.getId(),
                output.getPortfolioId(),
                output.getAssetId(),
                output.getType(),
                output.getQuantity(),
                output.getStatus(),
                output.getCreatedAt()
        ));
    }

    private PortfolioResponse toPortfolioResponse(PortfolioOutput output) {
        var assetResponses = output.getAssets().stream()
                .map(a -> new PortfolioAssetResponse(a.getId(), a.getAssetId(), a.getTicker(), a.getAssetName(), a.getQuantity()))
                .collect(Collectors.toList());
        return new PortfolioResponse(output.getId(), output.getUserId(), assetResponses);
    }
}
