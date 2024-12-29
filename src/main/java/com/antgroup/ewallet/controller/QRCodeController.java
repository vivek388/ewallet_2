package com.antgroup.ewallet.controller;

import com.antgroup.ewallet.model.entity.PaymentCodeEnv;
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

            // Get user ID from cookie
            String userId = extractUserIdFromCookie(request);
            if (userId == null) {
                logger.error("User ID not found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }
            logger.debug("Generating QR code for user ID: {}", userId);

            // Get current time in UTC
            String requestTime = LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'"));

            // Create payment code request
            PaymentCodeRequest paymentCodeRequest = createPaymentCodeRequest(userId);

            // Generate signature
            String requestJson = objectMapper.writeValueAsString(paymentCodeRequest);
            String signature = aliPayService.getSignature(PAYMENT_CODE_URI, requestTime, requestJson);

            logger.debug("Generated signature for request");

            // Call AliPay API
            PaymentCodeResponse response = aliPayService.getPaymentCode(
                    PAYMENT_CODE_URL,
                    paymentCodeRequest,
                    requestTime,
                    signature
            );

            if (response == null || response.getResult() == null) {
                logger.error("Received null response from payment code API");
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

            // Generate QR code
            String htmlResponse = generateQrCodeHtml(response);

            logger.info("Successfully generated QR code");
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
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private PaymentCodeRequest createPaymentCodeRequest(String userId) {
        return new PaymentCodeRequest(
                "PH",           // region
                userId,         // customerId
                "1",           // codeQuantity
                new PaymentCodeEnv("device_" + userId)  // deviceTokenId
        );
    }

    private String generateQrCodeHtml(PaymentCodeResponse response) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                response.getPaymentCodeInfoList().get(0).getPaymentCode(),
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        String qrCodeImage = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

        return String.format(
                "<div style='text-align:center;'>" +
                        "<img src='data:image/png;base64,%s' alt='Payment QR Code'/>" +
                        "<p>Valid from: %s</p>" +
                        "<p>Expires at: %s</p>" +
                        "<button onclick='window.location.href=\"/wallet\"' " +
                        "style='padding: 10px 20px; margin-top: 20px; background-color: #007bff; " +
                        "color: white; border: none; border-radius: 5px; cursor: pointer;'>" +
                        "Back to Wallet</button>" +
                        "</div>",
                qrCodeImage,
                response.getPaymentCodeInfoList().get(0).getCodeValidityStartTime(),
                response.getPaymentCodeInfoList().get(0).getCodeExpiryTime()
        );
    }
}