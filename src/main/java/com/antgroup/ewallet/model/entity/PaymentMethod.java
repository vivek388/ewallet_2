package com.antgroup.ewallet.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentMethod {
    @JsonProperty("paymentMethodType")
    private String paymentMethodType; // Should be "CONNECT_WALLET"

    @JsonProperty("paymentMethodId")
    private String paymentMethodId;   // The payment code

    @JsonProperty("customerId")
    private String customerId;        // User ID

    // Default constructor
    public PaymentMethod() {}

    // Constructor with all fields
    public PaymentMethod(String paymentMethodType, String paymentMethodId, String customerId) {
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodId = paymentMethodId;
        this.customerId = customerId;
    }

    // Getters and Setters
    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public void setPaymentMethodType(String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}