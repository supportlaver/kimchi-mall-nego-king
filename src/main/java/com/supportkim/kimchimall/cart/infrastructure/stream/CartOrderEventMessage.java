package com.supportkim.kimchimall.cart.infrastructure.stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CartOrderEventMessage {
    private Long cartId;
    private Map<Long , Integer> productIdsToQuantities = new HashMap<>();
    private Long buyerId;
    private String seed;
    private String idempotencyKey;
}
