package com.custody.custody_service.domain.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "assets")
public class Asset implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    public Asset() {}

    public Asset(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
