package com.antgroup.ewallet.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentCodeEnv {
    private String deviceTokenId;

    // Default constructor
    public PaymentCodeEnv() {
    }

    @JsonCreator
    public PaymentCodeEnv(@JsonProperty("deviceTokenId") String deviceTokenId) {
        this.deviceTokenId = deviceTokenId;
    }

    public String getDeviceTokenId() {
        return deviceTokenId;
    }

    public void setDeviceTokenId(String deviceTokenId) {
        this.deviceTokenId = deviceTokenId;
    }
}