package com.supportkim.kimchimall.cart.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartRequestDto {
    @Builder @Getter
    @AllArgsConstructor @NoArgsConstructor
    public static class AddToCartRequest {
        private Long kimchiId;
        private int quantity;
    }

    @Builder @Getter
    @AllArgsConstructor @NoArgsConstructor
    public static class CartOrderEventMessageDto {
        private Map<Long , Integer> productIdsToQuantities = new HashMap<>();
        private Long cartId;
        private Long buyerId;
        private String seed;
        private String idempotencyKey;

        public static CartOrderEventMessageDto of(Long buyerId , Long cartId , Map<Long , Integer> productIdsToQuantities) {
            return CartOrderEventMessageDto.builder()
                    .buyerId(buyerId)
                    .cartId(cartId)
                    .productIdsToQuantities(productIdsToQuantities)
                    .seed(LocalDateTime.now().toString())
                    .idempotencyKey(UUID.randomUUID().toString())
                    .build();
        }
    }
}
