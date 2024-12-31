package com.supportkim.kimchimall.order.infrastructure.stream;

import com.supportkim.kimchimall.orderkimchi.domain.OrderKimchi;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class OrderEventMessage {
    private Long memberId;
    private List<OrderKimchi> orderKimchis;
    private int totalPrice;
    private String idempotencyKey;

    public static OrderEventMessage of(Long memberId , List<OrderKimchi> orderKimchis , int totalPrice , String idempotencyKey) {
        return OrderEventMessage.builder()
                .memberId(memberId)
                .orderKimchis(orderKimchis)
                .totalPrice(totalPrice)
                .idempotencyKey(idempotencyKey)
                .build();
    }

}
