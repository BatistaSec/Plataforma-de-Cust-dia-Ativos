package com.custody.custody_service.infrastructure.persistence.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioAssetEntity> assets = new ArrayList<>();

    public PortfolioEntity() {}

    public PortfolioEntity(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<PortfolioAssetEntity> getAssets() { return assets; }
    public void setAssets(List<PortfolioAssetEntity> assets) { this.assets = assets; }
}
