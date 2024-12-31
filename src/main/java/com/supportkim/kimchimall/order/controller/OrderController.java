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

    /**
     * 주문하기 로직 추가하기
     * 주문 로직에서 Kafka or WebFlux+R2DBC 를 사용해서 비동기적으로 프로그래밍 후 성능 최적화 하기
     * -> order() 하면 Kafka 에 던지고 일단 주문 완료! 이렇게 하는건 어떤가..?
     * 그 다음에 결제 로직 추가하면서 MSA 환경으로 만들기
     * Order 부분 부터 만들기
     */

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
