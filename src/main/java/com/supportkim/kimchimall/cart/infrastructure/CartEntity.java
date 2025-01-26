package com.supportkim.kimchimall.cart.infrastructure;

import com.supportkim.kimchimall.cart.controller.port.CartService;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cartkimchi.infrastructure.CartKimchiEntity;
import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static java.util.stream.Collectors.*;

@Entity
@Table(name = "carts")
@Getter @Builder @Slf4j
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToMany(cascade = ALL , orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<CartKimchiEntity> cartKimchiEntityList = new ArrayList<>();
    public static CartEntity from(Cart cart) {
        return CartEntity.builder()
                .id(cart.getId())
                .cartKimchiEntityList(cart.getCartKimchis().stream().map(CartKimchiEntity::from).collect(toList()))
                .build();
    }
    public Cart toModel() {
        return Cart.builder()
                .id(id)
                .cartKimchis(cartKimchiEntityList.stream().map(CartKimchiEntity::toModel).collect(toList()))
                .build();
    }
}
