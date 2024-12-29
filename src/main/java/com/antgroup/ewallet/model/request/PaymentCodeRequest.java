package com.antgroup.ewallet.model.request;

import com.antgroup.ewallet.model.entity.*;

public class PaymentCodeRequest {
    public PaymentCodeRequest(String region, String customerId, String codeQuantity, PaymentCodeEnv env) {
        this.region = region;
        this.customerId = customerId;
        this.codeQuantity = codeQuantity;
        this.env = env;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCodeQuantity() {
        return codeQuantity;
    }

    public void setCodeQuantity(String codeQuantity) {
        this.codeQuantity = codeQuantity;
    }

    public PaymentCodeEnv getEnv() {
        return env;
    }

    public void setEnv(PaymentCodeEnv env) {
        this.env = env;
    }

    private String region;
    private String customerId;
    private String codeQuantity;
    private PaymentCodeEnv env;

    // Include getter and setter methods for each field
}