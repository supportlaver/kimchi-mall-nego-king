package com.supportkim.kimchimall.cart.controller;

import com.supportkim.kimchimall.cart.controller.port.CartService;
import com.supportkim.kimchimall.common.global.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.supportkim.kimchimall.cart.controller.request.CartRequestDto.*;
import static com.supportkim.kimchimall.cart.controller.response.CartResponseDto.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // 장바구니 담기
    @PostMapping("/{kimchi-id}")
    public ResponseEntity<BaseResponse<AddToCartResponse>> putCart(@RequestParam("quantity") int quantity,
                                                                   @PathVariable("kimchi-id") Long kimchiId,
                                                                   HttpServletRequest request) {
        return ResponseEntity.ok().body(new BaseResponse<>(cartService.addToCart(quantity , kimchiId , request)));
    }

    // 장바구니에서 결제 요청
    @PostMapping("/payment")
    public ResponseEntity<BaseResponse<String>> requestPayment(HttpServletRequest request) {
        return ResponseEntity.ok().body(new BaseResponse<>(cartService.requestPayment(request)));
    }
}
