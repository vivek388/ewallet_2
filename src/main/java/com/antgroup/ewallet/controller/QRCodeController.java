package com.antgroup.ewallet.controller;

import com.antgroup.ewallet.model.entity.PaymentCodeEnv;
import com.antgroup.ewallet.model.entity.PaymentCodeInfo;
import com.antgroup.ewallet.model.request.PaymentCodeRequest;
import com.antgroup.ewallet.model.response.PaymentCodeResponse;
import com.antgroup.ewallet.service.AliPayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@RestController
@RequestMapping("/wallet")
public class QRCodeController {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeController.class);
    private static final String PAYMENT_CODE_URI = "/aps/api/v1/codes/getPaymentCode";
    private static final String PAYMENT_CODE_URL = "https://open-sea.alipayplus.com/aps/api/v1/codes/getPaymentCode";

    @Value("${alipay.clientId}")
    private String clientId;

    @Value("${alipay.region:US}")  // Default to ID if not specified
    private String region;

    @Value("${alipay.merchant.id}")
    private String merchantId;

    private final AliPayService aliPayService;
    private final ObjectMapper objectMapper;

    public QRCodeController(AliPayService aliPayService, ObjectMapper objectMapper) {
        this.aliPayService = aliPayService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/generateQrCode")
    public ResponseEntity<String> generateQrCode(HttpServletRequest request) {
        try {
            logger.info("Starting QR code generation process");

            String userId = extractUserIdFromCookie(request);
            if (userId == null) {
                logger.error("User ID not found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }
            logger.debug("User ID extracted from cookie: {}", userId);

            String requestTime = LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'"));

            logger.debug("Generated request time: {}", requestTime);

            // Create payment code request with proper logging
            PaymentCodeRequest paymentCodeRequest = new PaymentCodeRequest(
                    region,
                    userId,  // Original user ID from cookie
                    "1",    // Default quantity
                    new PaymentCodeEnv(merchantId)
            );

            // Log the complete request details
            logger.info("Created PaymentCodeRequest with values:");
            logger.info("Region: {}", paymentCodeRequest.getRegion());
            logger.info("Customer ID: {}", paymentCodeRequest.getCustomerId());
            logger.info("Code Quantity: {}", paymentCodeRequest.getCodeQuantity());
            logger.info("Device Token ID: {}", paymentCodeRequest.getEnv().getDeviceTokenId());

            String requestJson = objectMapper.writeValueAsString(paymentCodeRequest);
            logger.debug("Request JSON: {}", requestJson);

            String signature = aliPayService.getSignature(PAYMENT_CODE_URI, requestTime, requestJson);
            logger.debug("Generated signature: {}", signature);

            PaymentCodeResponse response = aliPayService.getPaymentCode(
                    PAYMENT_CODE_URL,
                    paymentCodeRequest,
                    requestTime,
                    signature
            );

            logger.info("Received response from AliPay API");
            logger.debug("Response details: {}", objectMapper.writeValueAsString(response));

            if (response == null || response.getResult() == null) {
                logger.error("Null response from payment code API");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate QR code: No response from payment service");
            }

            if (!"S".equals(response.getResult().getResultStatus())) {
                logger.error("Error response from payment code API: {}", response.getResult().getResultMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to generate QR code: " + response.getResult().getResultMessage());
            }

            if (response.getPaymentCodeInfoList() == null || response.getPaymentCodeInfoList().isEmpty()) {
                logger.error("No payment code info in response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate QR code: No payment code generated");
            }

            PaymentCodeInfo codeInfo = response.getPaymentCodeInfoList().get(0);
            logger.info("Successfully extracted payment code info");
            logger.debug("Payment code validity: {} to {}",
                    codeInfo.getCodeValidityStartTime(),
                    codeInfo.getCodeExpiryTime());

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    codeInfo.getPaymentCode(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            String qrCodeImage = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

            logger.info("Successfully generated QR code image");

            String htmlResponse = String.format(
                    "<div class='qr-code-content'>" +
                            "<h3>Your Payment QR Code</h3>" +
                            "<img src='data:image/png;base64,%s' alt='Payment QR Code'/>" +
                            "<p>Valid from: %s</p>" +
                            "<p>Expires at: %s</p>" +
                            "<button onclick=\"document.getElementById('qrCodeContainer').style.display='none'\" " +
                            "class='close-button'>Close</button>" +
                            "</div>",
                    qrCodeImage,
                    codeInfo.getCodeValidityStartTime(),
                    codeInfo.getCodeExpiryTime()
            );

            logger.info("QR code generation process completed successfully");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlResponse);

        } catch (Exception e) {
            logger.error("Error generating QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating QR code: " + e.getMessage());
        }
    }

    private String extractUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ewalletID".equals(cookie.getName())) {
                    logger.debug("Found ewalletID cookie with value: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        logger.warn("No ewalletID cookie found in request");
        return null;
    }
}