package com.antgroup.ewallet.model.response;

import com.antgroup.ewallet.model.entity.*;
import java.util.List;

public class PaymentCodeResponse {
    private BaseResult result;
    private List<PaymentCodeInfo> paymentCodeInfoList;

    // Default constructor required by Jackson
    public PaymentCodeResponse() {
    }

    public PaymentCodeResponse(BaseResult result, List<PaymentCodeInfo> paymentCodeInfoList) {
        this.result = result;
        this.paymentCodeInfoList = paymentCodeInfoList;
    }

    public List<PaymentCodeInfo> getPaymentCodeInfoList() {
        return paymentCodeInfoList;
    }

    public void setPaymentCodeInfoList(List<PaymentCodeInfo> paymentCodeInfoList) {
        this.paymentCodeInfoList = paymentCodeInfoList;
    }

    public BaseResult getResult() {
        return result;
    }

    public void setResult(BaseResult result) {
        this.result = result;
    }
}