package com.supportkim.kimchimall.payment.service.dto;

import com.supportkim.kimchimall.common.util.IdempotencyCreator;
import com.supportkim.kimchimall.payment.controller.request.CheckoutRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data @Builder
public class CheckoutCommand {
    private Long cartId;
    private Long buyerId;
    private List<Long> kimchiIds;
    private String idempotencyKey;

    public static CheckoutCommand from(CheckoutRequest request) {
        return CheckoutCommand.builder()
                .cartId(request.getCartId())
                .buyerId(request.getBuyerId())
                .kimchiIds(request.getKimchiIds())
                // .idempotencyKey(IdempotencyCreator.create(request.getSeed()))
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
    }
}
