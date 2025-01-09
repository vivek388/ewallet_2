package com.antgroup.ewallet.model.response;

import com.antgroup.ewallet.model.entity.BaseResult;
import com.antgroup.ewallet.model.entity.PaymentCodeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PaymentCodeResponse {
    @JsonProperty("result")
    private BaseResult result;

    @JsonProperty("paymentCodeInfoList")
    private List<PaymentCodeInfo> paymentCodeInfoList;

    // Default constructor
    public PaymentCodeResponse() {
    }

    public PaymentCodeResponse(BaseResult result, List<PaymentCodeInfo> paymentCodeInfoList) {
        this.result = result;
        this.paymentCodeInfoList = paymentCodeInfoList;
    }

    public BaseResult getResult() {
        return result;
    }

    public void setResult(BaseResult result) {
        this.result = result;
    }

    public List<PaymentCodeInfo> getPaymentCodeInfoList() {
        return paymentCodeInfoList;
    }

    public void setPaymentCodeInfoList(List<PaymentCodeInfo> paymentCodeInfoList) {
        this.paymentCodeInfoList = paymentCodeInfoList;
    }
}