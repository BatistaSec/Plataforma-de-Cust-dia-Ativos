package com.custody.custody_service.presentation.controllers;

import com.custody.custody_service.application.services.CustodyService;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.presentation.dtos.CustodyOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/custody")
public class CustodyController {

    private final CustodyService custodyService;

    public CustodyController(CustodyService custodyService) {
        this.custodyService = custodyService;
    }

    @PostMapping("/portfolios")
    public ResponseEntity<Portfolio> createPortfolio(@RequestParam UUID userId) {
        return ResponseEntity.ok(custodyService.createPortfolio(userId));
    }

    @GetMapping("/portfolios/user/{userId}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable UUID userId) {
        return ResponseEntity.ok(custodyService.getPortfolioByUserId(userId));
    }

    @PostMapping("/assets")
    public ResponseEntity<Asset> createAsset(@RequestParam String ticker, @RequestParam String name) {
        return ResponseEntity.ok(custodyService.createAsset(ticker, name));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(custodyService.getAllAssets());
    }

    @PostMapping("/orders")
    public ResponseEntity<CustodyOrder> createOrder(@Valid @RequestBody CustodyOrderRequest request) {
        CustodyOrder order = custodyService.createOrder(
                request.getPortfolioId(),
                request.getAssetId(),
                request.getType(),
                request.getQuantity()
        );
        return ResponseEntity.ok(order);
    }
}
