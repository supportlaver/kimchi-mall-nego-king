package com.supportkim.kimchimall.cart.service.port;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.member.domain.Member;

public interface CartRepository {
    Cart save(Cart cart);
}
