package com.antgroup.ewallet.model.response;

import com.antgroup.ewallet.model.entity.BaseResult;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponse {
    @JsonProperty("result")
    private BaseResult result;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("paymentTime")
    private String paymentTime;

    @JsonProperty("customerId")
    private String customerId;

    // Default constructor
    public PaymentResponse() {}

    // Constructor with all fields
    public PaymentResponse(BaseResult result, String paymentId, String paymentTime, String customerId) {
        this.result = result;
        this.paymentId = paymentId;
        this.paymentTime = paymentTime;
        this.customerId = customerId;
    }

    // Getters and Setters
    public BaseResult getResult() {
        return result;
    }

    public void setResult(BaseResult result) {
        this.result = result;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}