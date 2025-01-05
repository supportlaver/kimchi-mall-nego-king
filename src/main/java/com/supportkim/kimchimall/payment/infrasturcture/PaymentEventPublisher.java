package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@Slf4j
public class PaymentEventPublisher {
    private final Sinks.Many<PaymentEventMessage> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<PaymentEventMessage>> payment() {
        return () -> sink.asFlux()
                .doOnError(error -> {
                    // 에러 처리 로직
                    System.err.println("Error in couponEventSupplier: " + error.getMessage());
                });
    }

    public void publishToPaymentTopic(PaymentEventMessage event) {
        log.info("JIWON : PaymentTopic 에 event 전달 = {} " , event.getOrderId());
        sink.tryEmitNext(event);
    }
}
