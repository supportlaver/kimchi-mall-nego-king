package com.supportkim.kimchimall.cart.infrastructure.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalEventPublisher;
import reactor.core.publisher.Mono;

/**
 * MessagePublisher 로 보내는 방법과 StreamBridge 로 보내는 방법이 있습니다.
 * 2개를 모두 고려해봐야할 사항이며, 우선 MessagePublisher 를 사용했습니다. (둘다 비동기식)
 */
// @Component
@Slf4j
public class CartPaymentMessagePublisher {
    private final TransactionalEventPublisher transactionalEventPublisher;
    public CartPaymentMessagePublisher(ApplicationEventPublisher publisher) {
        // 생성자에서 TransactionalEventPublisher를 초기화
        this.transactionalEventPublisher = new TransactionalEventPublisher(publisher);
    }
    public Mono<CartOrderEventMessage> publishEvent(CartOrderEventMessage eventMessage) {
        log.info("JIWON : publishing event : {} " , eventMessage);
        Mono<CartOrderEventMessage> cartOrderEventMessageMono = transactionalEventPublisher.publishEvent(eventMessage).thenReturn(eventMessage);
        log.info("publishEvent 성공");
        return cartOrderEventMessageMono;
    }
}
