package com.custody.custody_service.infrastructure.persistence.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "assets")
public class AssetEntity implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer scale = 5;

    public AssetEntity() {}

    public AssetEntity(UUID id, String ticker, String name) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    public AssetEntity(UUID id, String ticker, String name, Integer scale) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.scale = scale != null ? scale : 5;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getScale() { return scale; }
    public void setScale(Integer scale) { this.scale = scale; }
}
