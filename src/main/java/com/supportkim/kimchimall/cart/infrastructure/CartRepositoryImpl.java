package com.supportkim.kimchimall.cart.infrastructure;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository @Slf4j
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Cart save(Cart cart) {
        CartEntity savedCart = CartEntity.from(cart);
        return cartJpaRepository.save(savedCart).toModel();
    }
}
