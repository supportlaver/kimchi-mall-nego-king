package com.supportkim.kimchimall.order.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRequestDto {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class OrderRequestImmediately {
        private Long kimchiId;
        private int count;
        private String kimchiName;
    }
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class CheckoutRequest {

        @JsonProperty("cartId")
        private Long cartId;

        @JsonProperty("productIds")
        private List<Long> productIds;

        @JsonProperty("buyerId")
        private Long buyerId;

        @JsonProperty("seed")
        private String seed;
        public static CheckoutRequest of(Long cartId , Long memberId , List<Long> kimchiIds) {
            return CheckoutRequest.builder()
                    .buyerId(memberId)
                    .cartId(cartId)
                    .productIds(kimchiIds)
                    .seed(LocalDateTime.now().toString())
                    .build();
        }
    }
}
