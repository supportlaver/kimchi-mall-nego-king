package com.supportkim.kimchimall.payment.controller.response;

import com.supportkim.kimchimall.payment.infrasturcture.PaymentEvent;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CheckoutResponse {
    private String orderId;
    private String orderName;
    private int amount;

    public static CheckoutResponse from(PaymentEvent paymentEvent) {
        return CheckoutResponse.builder()
                .orderId(paymentEvent.getOrderId())
                .orderName(paymentEvent.getOrderName())
                .amount(paymentEvent.totalAmount())
                .build();
    }
}
