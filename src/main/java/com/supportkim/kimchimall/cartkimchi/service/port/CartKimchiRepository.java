package com.supportkim.kimchimall.cartkimchi.service.port;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;

public interface CartKimchiRepository {
    CartKimchi save(CartKimchi cartKimchi , Cart cart);
}
