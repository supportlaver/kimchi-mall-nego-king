package com.supportkim.kimchimall.cartkimchi.domain;

import com.supportkim.kimchimall.cart.controller.request.CartRequestDto;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartKimchi {
    private Long id;
    private Kimchi kimchi;
    private int quantity;
    public static CartKimchi of(Kimchi kimchi , int quantity) {
        return CartKimchi.builder()
                .kimchi(kimchi)
                .quantity(quantity)
                .build();
    }
    @Override
    public String toString() {
        return "CartKimchi{" +
                "id=" + id +
                ", kimchi=" + kimchi +
                ", quantity=" + quantity +
                '}';
    }
}
