package com.supportkim.kimchimall.payment.controller.request;

import lombok.Data;

@Data
public class TossPaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
}
