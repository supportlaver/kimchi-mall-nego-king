package com.supportkim.kimchimall.order.controller.port;

import com.supportkim.kimchimall.cart.controller.request.CartRequestDto;
import com.supportkim.kimchimall.order.controller.request.OrderRequestDto;
import com.supportkim.kimchimall.order.controller.response.OrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

import static com.supportkim.kimchimall.cart.controller.request.CartRequestDto.*;
import static com.supportkim.kimchimall.order.controller.request.OrderRequestDto.*;
import static com.supportkim.kimchimall.order.controller.response.OrderResponseDto.*;

public interface OrderService {
    Mono<String> order(CartOrderEventMessageDto message);
    OrderResponse onlyOrder(HttpServletRequest request);
    OrderResponse orderImmediately(OrderRequestImmediately requestDto,HttpServletRequest request) throws InterruptedException;
}
