package com.antgroup.ewallet.model.entity;

public class PaymentCodeInfo {
    private String paymentCode;

    public PaymentCodeInfo(String paymentCode, String codeValidityStartTime, String codeExpiryTime) {
        this.paymentCode = paymentCode;
        this.codeValidityStartTime = codeValidityStartTime;
        this.codeExpiryTime = codeExpiryTime;
    }

    private String codeValidityStartTime;
    private String codeExpiryTime;

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

    // Include getter and setter methods
}
