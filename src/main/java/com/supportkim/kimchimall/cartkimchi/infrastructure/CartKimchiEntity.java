package com.supportkim.kimchimall.cartkimchi.infrastructure;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.infrastructure.CartEntity;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.cartkimchi.service.port.CartKimchiRepository;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_kimchis")
@Getter
@Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartKimchiEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_kimchi_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kimchi_id")
    private KimchiEntity kimchi;

    // 장바구니에 담긴 수량
    private int quantity;

    public static CartKimchiEntity from(CartKimchi cartKimchi) {
        return CartKimchiEntity.builder()
                .id(cartKimchi.getId())
                .kimchi(KimchiEntity.from(cartKimchi.getKimchi()))
                .quantity(cartKimchi.getQuantity())
                .build();
    }

    public CartKimchi toModel() {
        return CartKimchi.builder()
                .id(id)
                .quantity(quantity)
                .kimchi(kimchi.toModel())
                .build();
    }

    @Override
    public String toString() {
        return "CartKimchiEntity{" +
                "id=" + id +
                ", kimchi=" + kimchi +
                ", quantity=" + quantity +
                '}';
    }
}
