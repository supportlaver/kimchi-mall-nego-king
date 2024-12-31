package com.supportkim.kimchimall.order.controller.port;

import com.supportkim.kimchimall.common.config.FeignConfig;
import com.supportkim.kimchimall.order.controller.request.OrderRequestDto;
import com.supportkim.kimchimall.order.controller.response.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.supportkim.kimchimall.order.controller.request.OrderRequestDto.*;

/**
 * Web Flux 와 통신하는 경우 FeignClient 방식은 적절하지 않을 수 있다.
 */
@FeignClient(name = "checkout-order-service" , url = "http://localhost:9000")
public interface CheckoutOrderService {
    @PostMapping("/")
    OrderResponseDto.CheckoutResponse sendCheckoutService(@RequestBody CheckoutRequest request);
}
