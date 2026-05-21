package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.PaymentResponse;
import com.RentKaro.RentKaro.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable Long bookingId,
            Authentication authentication) {
        PaymentResponse response = paymentService.processPayment(bookingId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> processRefund(
            @PathVariable Long bookingId,
            Authentication authentication) {
        PaymentResponse response = paymentService.processRefund(bookingId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }
}
