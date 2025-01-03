package com.supportkim.kimchimall.payment.service.dto;

import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentConfirmCommand {
    private String paymentKey;
    private String orderId;
    private int amount;

    public static PaymentConfirmCommand from(TossPaymentConfirmRequest request) {
        return PaymentConfirmCommand.builder()
                .paymentKey(request.getPaymentKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .build();
    }
}
