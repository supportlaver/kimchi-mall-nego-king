package com.supportkim.kimchimall.cart.controller.port;

import com.supportkim.kimchimall.cart.domain.Cart;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

import static com.supportkim.kimchimall.cart.controller.request.CartRequestDto.*;
import static com.supportkim.kimchimall.cart.controller.response.CartResponseDto.*;

public interface CartService {
    AddToCartResponse addToCart(int quantity , Long kimchiId , HttpServletRequest request);
    String requestPayment(HttpServletRequest request);
}
