package com.antgroup.ewallet.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SettlementStrategy {
    @JsonProperty("settlementCurrency")
    private String settlementCurrency;

    // Add constructor
    public SettlementStrategy(String settlementCurrency) {
        this.settlementCurrency = settlementCurrency;
    }

    // Getters and setters
    public String getSettlementCurrency() {
        return settlementCurrency;
    }

    public void setSettlementCurrency(String settlementCurrency) {
        this.settlementCurrency = settlementCurrency;
    }
}