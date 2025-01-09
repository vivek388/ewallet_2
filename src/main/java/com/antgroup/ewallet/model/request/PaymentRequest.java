package com.antgroup.ewallet.model.request;

import com.antgroup.ewallet.model.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequest {
    @JsonProperty("order")
    private Order order;

    @JsonProperty("acquirerId")
    private String acquirerId;

    @JsonProperty("pspId")
    private String pspId;

    @JsonProperty("paymentRequestId")
    private String paymentRequestId;

    @JsonProperty("paymentExpiryTime")
    private String paymentExpiryTime;

    @JsonProperty("paymentAmount")
    private BasePayment paymentAmount;

    @JsonProperty("paymentMethod")
    private PaymentMethod paymentMethod;

    @JsonProperty("payToAmount")
    private BasePayment payToAmount;

    @JsonProperty("paymentQuote")
    private PaymentQuote paymentQuote;

    @JsonProperty("paymentFactor")
    private PaymentFactor paymentFactor;

    // Default constructor
    public PaymentRequest() {}

    // Getters and Setters
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getPspId() {
        return pspId;
    }

    public void setPspId(String pspId) {
        this.pspId = pspId;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public String getPaymentExpiryTime() {
        return paymentExpiryTime;
    }

    public void setPaymentExpiryTime(String paymentExpiryTime) {
        this.paymentExpiryTime = paymentExpiryTime;
    }

    public BasePayment getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BasePayment paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BasePayment getPayToAmount() {
        return payToAmount;
    }

    public void setPayToAmount(BasePayment payToAmount) {
        this.payToAmount = payToAmount;
    }

    public PaymentQuote getPaymentQuote() {
        return paymentQuote;
    }

    public void setPaymentQuote(PaymentQuote paymentQuote) {
        this.paymentQuote = paymentQuote;
    }

    public PaymentFactor getPaymentFactor() {
        return paymentFactor;
    }

    public void setPaymentFactor(PaymentFactor paymentFactor) {
        this.paymentFactor = paymentFactor;
    }
}