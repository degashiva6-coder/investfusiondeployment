package com.investfusion;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity
public class Holding {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String name;
    private String symbol;
    private String isin;
    @NotBlank private String assetType = "STOCK";
    @PositiveOrZero private double quantity;
    @PositiveOrZero private double averageCost;
    @PositiveOrZero private double currentPrice;
    @PositiveOrZero private double statementValue;
    private String notes;
    private Instant updatedAt = Instant.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id") @JsonIgnore private AppUser owner;

    public Holding() {}
    public Holding(String name, String symbol, String isin, String assetType, double quantity, double averageCost, double currentPrice, double statementValue, String notes) {
        this.name=name; this.symbol=symbol; this.isin=isin; this.assetType=assetType; this.quantity=quantity; this.averageCost=averageCost; this.currentPrice=currentPrice; this.statementValue=statementValue; this.notes=notes;
    }
    @PreUpdate @PrePersist void touch() { updatedAt = Instant.now(); }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getSymbol(){return symbol;} public void setSymbol(String v){symbol=v;}
    public String getIsin(){return isin;} public void setIsin(String v){isin=v;}
    public String getAssetType(){return assetType;} public void setAssetType(String v){assetType=v;}
    public double getQuantity(){return quantity;} public void setQuantity(double v){quantity=v;}
    public double getAverageCost(){return averageCost;} public void setAverageCost(double v){averageCost=v;}
    public double getCurrentPrice(){return currentPrice;} public void setCurrentPrice(double v){currentPrice=v;}
    public double getStatementValue(){return statementValue;} public void setStatementValue(double v){statementValue=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
    public AppUser getOwner(){return owner;} public void setOwner(AppUser v){owner=v;}
    public double getInvestedValue(){return quantity * averageCost;}
    public double getMarketValue(){return quantity * currentPrice;}
    public double getProfitLoss(){return getMarketValue() - getInvestedValue();}
    public double getProfitLossPercent(){return getInvestedValue() == 0 ? 0 : getProfitLoss() / getInvestedValue() * 100;}
}
