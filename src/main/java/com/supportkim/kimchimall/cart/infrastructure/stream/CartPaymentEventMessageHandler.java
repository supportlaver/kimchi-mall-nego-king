package com.supportkim.kimchimall.cart.infrastructure.stream;

import com.supportkim.kimchimall.cart.controller.port.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * 패키지 구조는 전체적으로 수정할 예정
 * 해당 Handler 에서 "cart-payment" 토픽에 Message 를 송신합니다.
 * payment-server 에서 해당 Message 를 수신하여 결제 로직을 처리합니다.
 */
// TODO: 9/13/24 현재는 ApplicationEventPublisher 를 통해 전송하고 있고 StreamBridge 를 사용하는 경우 해당 클래스에서 설정할 예정
@Configuration
@RequiredArgsConstructor
public class CartPaymentEventMessageHandler {

    private final StreamBridge streamBridge;
    private final CartService cartService;

}
