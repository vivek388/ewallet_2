package com.antgroup.ewallet.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentCodeInfo {
    @JsonProperty("paymentCode")
    private String paymentCode;

    @JsonProperty("codeValidityStartTime")
    private String codeValidityStartTime;

    @JsonProperty("codeExpiryTime")
    private String codeExpiryTime;

    // Default constructor required by Jackson
    public PaymentCodeInfo() {
    }

    public PaymentCodeInfo(String paymentCode, String codeValidityStartTime, String codeExpiryTime) {
        this.paymentCode = paymentCode;
        this.codeValidityStartTime = codeValidityStartTime;
        this.codeExpiryTime = codeExpiryTime;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getCodeValidityStartTime() {
        return codeValidityStartTime;
    }

    public void setCodeValidityStartTime(String codeValidityStartTime) {
        this.codeValidityStartTime = codeValidityStartTime;
    }

    public String getCodeExpiryTime() {
        return codeExpiryTime;
    }

    public void setCodeExpiryTime(String codeExpiryTime) {
        this.codeExpiryTime = codeExpiryTime;
    }
}