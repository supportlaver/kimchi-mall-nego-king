package com.supportkim.kimchimall.kimchi.domain;

import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Kimchi {
    private Long id;
    private String name;
    private int price;
    private KimchiType type;
    private int quantity;
    private List<CartKimchi> cartKimchis;

    public synchronized void decreaseQuantity(int quantity) {
        this.quantity = this.quantity - quantity;
    }
}
