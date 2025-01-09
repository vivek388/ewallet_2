package com.antgroup.ewallet.controller;

import com.antgroup.ewallet.model.entity.*;
import com.antgroup.ewallet.model.request.*;
import com.antgroup.ewallet.model.response.*;
import com.antgroup.ewallet.service.AliPayService;
import com.antgroup.ewallet.service.ExcelService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@RestController
public class AliPayController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${alipay.clientId}")
    private String clientId;

    // Constants for API URLs and URIs
    private static final String USER_INITIATED_PAY_URL = "https://open-sea-global.alipayplus.com/aps/api/v1/payments/userInitiatedPay";
    private static final String USER_INITIATED_PAY_URI = "/aps/api/v1/payments/userInitiatedPay";
    private static final String NOTIFY_PAYMENT_URL = "https://open-sea-global.alipayplus.com/aps/api/v1/payments/notifyPayment";
    private static final String NOTIFY_PAYMENT_URI = "/aps/api/v1/payments/notifyPayment";
    private static final String PAYMENT_CODE_URL = "https://open-sea.alipayplus.com/aps/api/v1/codes/getPaymentCode";
    private static final String PAYMENT_CODE_URI = "/aps/api/v1/codes/getPaymentCode";
    private static final String PAY_URL = "https://open-sea-global.alipayplus.com/aps/api/v1/payments/pay";
    private static final String PAY_URI = "/aps/api/v1/payments/pay";
    private static final String ORDER_IS_CLOSED = "ORDER_IS_CLOSED";
    private static final String COOKIE_NAME = "ewalletID";

    private final AliPayService aliPayService;
    private final ExcelService excelService;
    private final ObjectMapper objectMapper;

    public AliPayController(AliPayService aliPayService, ExcelService excelService, ObjectMapper objectMapper) {
        this.aliPayService = aliPayService;
        this.excelService = excelService;
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @PostMapping("/initiatePay")
    public UserInitiatedPayResponse initiatePay(@RequestBody QrCodeRequest qrCodeRequest,
                                                HttpServletRequest request) throws JsonProcessingException {
        logger.info("Received request to initiate payment. QR Code: {}", qrCodeRequest.getCode());

        String userId = extractUserIdFromCookie(request);
        logger.debug("Customer ID retrieved from cookie: {}", userId);

        String formattedDateTime = getCurrentFormattedDateTime();
        UserInitiatedPayRequest userInitiatedPayRequest = new UserInitiatedPayRequest();
        userInitiatedPayRequest.setCodeValue(qrCodeRequest.getCode());
        userInitiatedPayRequest.setCustomerId(userId);

        String userInitiatedPayJson = objectMapper.writeValueAsString(userInitiatedPayRequest);
        String signature = aliPayService.getSignature(USER_INITIATED_PAY_URI, formattedDateTime, userInitiatedPayJson);

        logger.debug("Generated API signature for UserInitiatedPay request: {}", signature);
        UserInitiatedPayResponse response = aliPayService.UserInitiatedPay(USER_INITIATED_PAY_URL,
                userInitiatedPayRequest, formattedDateTime, signature);

        logger.info("Received UserInitiatedPay response: {}", response);
        return response;
    }

    @PostMapping("/initiatedPay/pay")
    public WalletApiResult payInitiatedPay(@RequestBody UserInitiatedPayResponse initiatedPayResponse,
                                           HttpServletRequest request) throws JsonProcessingException {
        logger.info("Processing payment for response: {}", initiatedPayResponse);

        String userId = extractUserIdFromCookie(request);
        User user = excelService.getUserById(userId);
        String formattedDateTime = getCurrentFormattedDateTime();

        double payToAmount = Double.parseDouble(initiatedPayResponse.payToAmount.value) / 100;
        double paymentId = -1;
        ApiResult apiResult = new ApiResult(new BaseResult("SUCCESS", "Success", "S"));

        if (user.getBalance() < payToAmount) {
            logger.warn("User balance insufficient. User ID: {}, Balance: {}, Required: {}",
                    userId, user.getBalance(), payToAmount);
            apiResult = new ApiResult(new BaseResult("USER_BALANCE_NOT_ENOUGH", "Insufficient Balance", "F"));
        } else {
            String promoJson = null;
            if (initiatedPayResponse.paymentPromoInfo != null) {
                promoJson = objectMapper.writeValueAsString(initiatedPayResponse.paymentPromoInfo);
            }

            paymentId = excelService.addTransaction(userId, -payToAmount,
                    initiatedPayResponse.order.orderDescription,
                    apiResult.result.resultCode,
                    apiResult.result.resultStatus,
                    apiResult.result.resultMessage,
                    initiatedPayResponse.paymentRequestId,
                    formattedDateTime,
                    initiatedPayResponse.paymentAmount,
                    initiatedPayResponse.payToAmount,
                    initiatedPayResponse.paymentQuote,
                    initiatedPayResponse.pspId,
                    initiatedPayResponse.acquirerId,
                    promoJson,
                    null
            );
            logger.info("Transaction recorded. Payment ID: {}", paymentId);
        }

        NotifyPaymentRequest notifyRequest = createNotifyPaymentRequest(apiResult, initiatedPayResponse,
                paymentId, formattedDateTime, userId);
        String notifyPaymentRequestJson = objectMapper.writeValueAsString(notifyRequest);
        String signature = aliPayService.getSignature(NOTIFY_PAYMENT_URI, formattedDateTime, notifyPaymentRequestJson);

        ApiResult response = aliPayService.NotifyPayment(NOTIFY_PAYMENT_URL, notifyRequest, formattedDateTime, signature);
        logger.info("Payment result - Status: {}, Message: {}", apiResult.result.resultCode, apiResult.result.resultMessage);

        return new WalletApiResult(response.result, paymentId);
    }

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponse> processPay(@RequestBody PaymentRequest paymentRequest,
                                                      @RequestHeader(value = "request-time", required = true) String requestTime,
                                                      @RequestHeader(value = "signature", required = true) String headerSignature,
                                                      HttpServletRequest request) throws JsonProcessingException {
        logger.info("Received pay request for ID: {}", paymentRequest.getPaymentRequestId());
        // Log the request details
        logger.info("Received pay request: {}", objectMapper.writeValueAsString(paymentRequest));
        logger.info("Request headers - request-time: {}, signature: {}", requestTime, headerSignature);

        String signature = extractSignature(headerSignature);
        String requestJson = objectMapper.writeValueAsString(paymentRequest);
        boolean verified = aliPayService.verify(PAY_URI, requestTime, requestJson, signature);

        if (!verified) {
            logger.error("Invalid signature for pay request");
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_SIGNATURE", "Invalid signature"));
        }

        if (!"CONNECT_WALLET".equals(paymentRequest.getPaymentMethod().getPaymentMethodType())) {
            logger.error("Invalid payment method type: {}", paymentRequest.getPaymentMethod().getPaymentMethodType());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_PAYMENT_METHOD", "Invalid payment method"));
        }

        String customerId = paymentRequest.getPaymentMethod().getCustomerId();
        double paymentAmount = Double.parseDouble(paymentRequest.getPayToAmount().getValue()) / 100;

        User user = excelService.getUserById(customerId);
        if (user == null) {
            logger.error("User not found: {}", customerId);
            return ResponseEntity.badRequest().body(createErrorResponse("USER_NOT_FOUND", "User not found"));
        }

        if (user.getBalance() < paymentAmount) {
            logger.error("Insufficient balance for user: {}", customerId);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("INSUFFICIENT_BALANCE", "Insufficient balance"));
        }

        String formattedDateTime = getCurrentFormattedDateTime();

        double transactionId = excelService.addTransaction(
                customerId,
                -paymentAmount,
                paymentRequest.getOrder().getOrderDescription(),
                "SUCCESS",
                "S",
                "Success",
                paymentRequest.getPaymentRequestId(),
                formattedDateTime,
                paymentRequest.getPaymentAmount(),
                paymentRequest.getPayToAmount(),
                paymentRequest.getPaymentQuote(),
                paymentRequest.getPspId(),
                paymentRequest.getAcquirerId(),
                null,
                null
        );

        PaymentResponse response = new PaymentResponse(
                new BaseResult("SUCCESS", "S", "success"),
                String.valueOf(transactionId),
                formattedDateTime,
                customerId
        );

        String responseJson = objectMapper.writeValueAsString(response);
        String responseSignature = aliPayService.getSignature(PAY_URI, formattedDateTime, responseJson);

        // Log API call
        logApiCall(request, requestJson, responseJson, verified);
        // Log the response details
        logger.info("Returning pay response: {}", objectMapper.writeValueAsString(response));
        logger.info("Response headers - Signature: {}, Response-Time: {}, Client-Id: {}",
                "algorithm=RSA256,keyVersion=1,signature=" + responseSignature,
                formattedDateTime,
                clientId);

        return ResponseEntity.ok()
                .header("Signature", "algorithm=RSA256,keyVersion=1,signature=" + responseSignature)
                .header("Response-Time", formattedDateTime)
                .header("Client-Id", clientId)
                .body(response);
    }

    @PostMapping("/getPaymentCode")
    public ResponseEntity<PaymentCodeResponse> getPaymentCodes(
            @RequestBody PaymentCodeRequest request,
            @RequestHeader(value = "request-time", required = true) String requestTime,
            @RequestHeader(value = "signature", required = true) String headerSignature,
            HttpServletRequest httpServletRequest) throws JsonProcessingException {

        // Log the request details
        logger.info("Received payment code request: {}", objectMapper.writeValueAsString(request));
        logger.info("Request headers - request-time: {}, signature: {}", requestTime, headerSignature);

        String extractedSignature = extractSignature(headerSignature);
        String requestJson = objectMapper.writeValueAsString(request);
        boolean verified = aliPayService.verify(PAYMENT_CODE_URI, requestTime, requestJson, extractedSignature);

        if (!verified) {
            logger.error("Invalid signature for payment code request");
            BaseResult baseResult = new BaseResult("INVALID_SIGNATURE", "The signature is invalid.", "F");
            return ResponseEntity.badRequest().body(new PaymentCodeResponse(baseResult, null));
        }

        PaymentCodeResponse response = aliPayService.getPaymentCode(PAYMENT_CODE_URL, request, requestTime, extractedSignature);
        String formattedDateTime = getCurrentFormattedDateTime();
        String resultJson = objectMapper.writeValueAsString(response);
        String responseSignature = aliPayService.getSignature(PAYMENT_CODE_URI, formattedDateTime, resultJson);

        // Log the response details
        logger.info("Returning payment code response: {}", objectMapper.writeValueAsString(response));
        logger.info("Response headers - Signature: {}, Response-Time: {}, Client-Id: {}",
                "algorithm=RSA256,keyVersion=1,signature=" + responseSignature,
                formattedDateTime,
                clientId);

        return ResponseEntity.ok()
                .header("Signature", "algorithm=RSA256,keyVersion=1,signature=" + responseSignature)
                .header("Response-Time", formattedDateTime)
                .header("Client-Id", clientId)
                .body(response);
    }

    @PostMapping("/inquiry")
    public ResponseEntity<InquiryPaymentResponse> inquiryPayment(
            @RequestBody InquiryPaymentRequest inquiryRequest,
            @RequestHeader(value = "request-time", required = true) String requestTime,
            @RequestHeader(value = "signature", required = true) String headerSignature,
            HttpServletRequest request) throws JsonProcessingException {

        logger.info("Received inquiry request for payment request ID: {}", inquiryRequest.getPaymentRequestId());

        String signature = extractSignature(headerSignature);
        String requestJson = objectMapper.writeValueAsString(inquiryRequest);
        boolean verified = aliPayService.verify("/inquiry", requestTime, requestJson, signature);

        if (!verified) {
            logger.error("Invalid signature for inquiry request");
            return ResponseEntity.badRequest().body(new InquiryPaymentResponse(
                    null, null, null, null, null, null,
                    new BaseResult("INVALID_SIGNATURE", "The signature is invalid.", "F")
            ));
        }

        List<Transaction> transactions = excelService.getAlipayTransactions(
                inquiryRequest.paymentRequestId,
                inquiryRequest.pspId,
                inquiryRequest.acquirerId
        );

        InquiryPaymentResponse response;
        if (transactions == null || transactions.isEmpty()) {
            response = new InquiryPaymentResponse(null, null, null, null, null, null,
                    new BaseResult("ORDER_NOT_EXIST", "The order doesn't exist.", "F"));
        } else {
            Transaction transaction = transactions.stream()
                    .min((t1, t2) -> t1.getDateTime().compareTo(t2.getDateTime()))
                    .orElseThrow();

            response = createInquiryResponse(transaction);
        }

        String formattedDateTime = getCurrentFormattedDateTime();
        String responseJson = objectMapper.writeValueAsString(response);
        String responseSignature = aliPayService.getSignature("/inquiry", formattedDateTime, responseJson);

        // Log API call
        logApiCall(request, requestJson, responseJson, verified);

        return ResponseEntity.ok()
                .header("Signature", "algorithm=RSA256,keyVersion=1,signature=" + responseSignature)
                .header("Response-Time", formattedDateTime)
                .header("Client-Id", clientId)
                .body(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResult> cancelPayment(
            @RequestBody CancelRequest cancelRequest,
            @RequestHeader(value = "request-time", required = true) String requestTime,
            @RequestHeader(value = "signature", required = true) String headerSignature,
            HttpServletRequest request) throws JsonProcessingException {

        logger.info("Received cancel request for payment request ID: {}", cancelRequest.getPaymentRequestId());

        String signature = extractSignature(headerSignature);
        String requestJson = objectMapper.writeValueAsString(cancelRequest);
        boolean verified = aliPayService.verify("/cancel", requestTime, requestJson, signature);

        if (!verified) {
            logger.error("Invalid signature for cancel request");
            return ResponseEntity.badRequest()
                    .body(new ApiResult(new BaseResult("INVALID_SIGNATURE", "The signature is invalid.", "F")));
        }

        List<Transaction> transactions = excelService.getAlipayTransactions(
                cancelRequest.paymentRequestId,
                cancelRequest.pspId,
                cancelRequest.acquirerId
        );

        ApiResult apiResult;
        if (transactions == null || transactions.isEmpty()) {
            apiResult = new ApiResult(new BaseResult("ORDER_NOT_EXIST", "The order doesn't exist.", "F"));
        } else {
            Transaction transaction = transactions.stream()
                    .min((t1, t2) -> t1.getDateTime().compareTo(t2.getDateTime()))
                    .orElseThrow();

            if (!transaction.getStatusCode().equals(ORDER_IS_CLOSED)) {
                logger.info("Cancelling transaction for paymentRequestId={}", cancelRequest.paymentRequestId);
                excelService.cancelTransaction(cancelRequest.paymentRequestId);
            }

            apiResult = new ApiResult(new BaseResult(true));
        }

        String formattedDateTime = getCurrentFormattedDateTime();
        String responseJson = objectMapper.writeValueAsString(apiResult);
        String responseSignature = aliPayService.getSignature("/cancel", formattedDateTime, responseJson);

        // Log API call
        logApiCall(request, requestJson, responseJson, verified);

        return ResponseEntity.ok()
                .header("Signature", "algorithm=RSA256,keyVersion=1,signature=" + responseSignature)
                .header("Response-Time", formattedDateTime)
                .header("Client-Id", clientId)
                .body(apiResult);
    }

    // Helper methods
    private String extractUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractSignature(String headerSignature) {
        return Arrays.stream(headerSignature.split(","))
                .filter(part -> part.startsWith("signature="))
                .map(part -> part.split("=")[1])
                .findFirst()
                .orElse(null);
    }

    private void logApiCall(HttpServletRequest request, String requestJson, String responseJson, boolean verified) {
        StringBuilder headersInfo = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            headersInfo.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        });

        excelService.addApiCallLog(
                request.getRequestURI(),
                "Body: " + requestJson + "\nHeaders: \n" + headersInfo.toString(),
                responseJson,
                verified
        );
    }

    private String getCurrentFormattedDateTime() {
        return LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'"));
    }

    private PaymentResponse createErrorResponse(String code, String message) {
        return new PaymentResponse(
                new BaseResult(code, "F", message),
                null,
                null,
                null
        );
    }

    private NotifyPaymentRequest createNotifyPaymentRequest(ApiResult apiResult,
                                                            UserInitiatedPayResponse initiatedPayResponse, double paymentId, String formattedDateTime, String userId) {
        return new NotifyPaymentRequest(
                apiResult.result,
                initiatedPayResponse.paymentRequestId,
                Double.toString(paymentId),
                formattedDateTime,
                initiatedPayResponse.paymentAmount,
                initiatedPayResponse.payToAmount,
                userId
        );
    }

    private InquiryPaymentResponse createInquiryResponse(Transaction transaction) {
        BaseResult baseResult = new BaseResult(
                transaction.getStatusCode(),
                transaction.getStatusMessage(),
                transaction.getStatus()
        );

        BasePayment payToAmount = new BasePayment(
                transaction.getPayToAmount(),
                transaction.getPayToCurrency()
        );

        BasePayment paymentAmount = new BasePayment(
                transaction.getPayAmount(),
                transaction.getPayCurrency()
        );

        return new InquiryPaymentResponse(
                transaction.getCustomerId(),
                payToAmount,
                paymentAmount,
                Double.toString(transaction.getId()),
                baseResult,
                transaction.getPaymentTime(),
                new BaseResult(true)
        );
    }
}