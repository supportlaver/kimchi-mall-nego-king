package com.supportkim.kimchimall.orderkimchi.domain;

import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.order.domain.Order;
import com.supportkim.kimchimall.order.infrastructure.OrderEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import static jakarta.persistence.FetchType.LAZY;

@Getter @Builder
public class OrderKimchi {
    // private Long id;
    private int price;
    private int quantity;
    private Kimchi kimchi;
    public static OrderKimchi from(CartKimchi cartKimchi) {
        return OrderKimchi.builder()
                .kimchi(cartKimchi.getKimchi())
                .quantity(cartKimchi.getQuantity())
                .price(cartKimchi.getKimchi().getPrice() * cartKimchi.getQuantity())
                .build();
    }
}
