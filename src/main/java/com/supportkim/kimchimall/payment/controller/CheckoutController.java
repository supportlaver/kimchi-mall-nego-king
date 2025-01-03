package com.supportkim.kimchimall.payment.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.common.util.IdempotencyCreator;
import com.supportkim.kimchimall.order.controller.request.OrderRequestDto;
import com.supportkim.kimchimall.payment.controller.request.CheckoutRequest;
import com.supportkim.kimchimall.payment.controller.response.CheckoutResponse;
import com.supportkim.kimchimall.payment.service.CheckoutService;
import com.supportkim.kimchimall.payment.service.dto.CheckoutCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class CheckoutController {
    private final CheckoutService checkoutService;

    // 구매자가 상품을 선택한 후 주문을 확인하고 결제를 준비
    @PostMapping("/check-out")
    public ResponseEntity<BaseResponse<CheckoutResponse>> checkoutPage(@RequestBody CheckoutRequest request) {
        log.info("request = {} " , request.toString());
        CheckoutCommand command = CheckoutCommand.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(checkoutService.checkout(command)));
    }
}
