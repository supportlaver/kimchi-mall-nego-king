package com.supportkim.kimchimall.order.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.order.controller.port.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.supportkim.kimchimall.order.controller.request.OrderRequestDto.*;
import static com.supportkim.kimchimall.order.controller.response.OrderResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/cart-order")
    public ResponseEntity<BaseResponse<OrderResponse>> order(HttpServletRequest request) {
        return ResponseEntity.ok().body(new BaseResponse<>(orderService.onlyOrder(request)));
    }

    @PostMapping("/order")
    public ResponseEntity<BaseResponse<OrderResponse>> orderImmediately(@RequestBody OrderRequestImmediately requestDto,
                                                                        HttpServletRequest request) throws InterruptedException {
        return ResponseEntity.ok().body(new BaseResponse<>(orderService.orderImmediately(requestDto,request)));
    }
}
