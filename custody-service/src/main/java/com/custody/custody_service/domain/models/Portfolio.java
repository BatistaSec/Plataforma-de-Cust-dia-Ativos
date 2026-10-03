package com.custody.custody_service.domain.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, unique = true)
    private UUID userId;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioAsset> assets = new ArrayList<>();

    public Portfolio() {}

    public Portfolio(UUID userId) {
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public List<PortfolioAsset> getAssets() { return assets; }
    public void setAssets(List<PortfolioAsset> assets) { this.assets = assets; }
    
    public void addAsset(PortfolioAsset asset) {
        assets.add(asset);
        asset.setPortfolio(this);
    }
}
