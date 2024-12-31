package com.supportkim.kimchimall.cartkimchi.infrastructure;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.infrastructure.CartEntity;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.cartkimchi.service.port.CartKimchiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository @Slf4j
@RequiredArgsConstructor
public class CartKimchiRepositoryImpl implements CartKimchiRepository {

    private final CartKimchiJpaRepository cartKimchiJpaRepository;
    private final CartRepository cartRepository;
    @Override
    public CartKimchi save(CartKimchi cartKimchi , Cart cart) {
        CartKimchi savedCartKimchi = cartKimchiJpaRepository.save(CartKimchiEntity.from(cartKimchi)).toModel();
        cart.getCartKimchis().add(savedCartKimchi);
        cartRepository.save(cart);
        return savedCartKimchi;
    }
}
