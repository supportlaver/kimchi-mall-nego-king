package com.supportkim.kimchimall.payment.controller.request;

import com.supportkim.kimchimall.payment.service.dto.PaymentConfirmCommand;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TossPaymentConfirmTest {
    private String paymentKey;
    private Long memberId;
    private String orderId;
    private int amount;

    public static TossPaymentConfirmTest from(TossPaymentConfirmTest request) {
        return TossPaymentConfirmTest.builder()
                .paymentKey(request.getPaymentKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .memberId(request.getMemberId())
                .build();
    }
}
