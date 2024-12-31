package com.supportkim.kimchimall.cart.controller.response;

import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CartResponseDto {

    @Builder @Getter
    @AllArgsConstructor @NoArgsConstructor
    public static class AddToCartResponse {
        private Long kimchiId;
        public static AddToCartResponse of(Kimchi kimchi) {
            return AddToCartResponse.builder()
                    .kimchiId(kimchi.getId())
                    .build();
        }
    }
}
