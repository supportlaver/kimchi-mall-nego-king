package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@Slf4j
public class PaymentEventPublisher {
    private final Sinks.Many<Message<PaymentEventMessage>> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<Message<PaymentEventMessage>>> payment() {
        return () -> sink.asFlux()
                .doOnError(error -> {
                    log.error("Error in couponEventSupplier: {}" , error.getMessage());
                });
    }

    public void publishToPaymentTopic(PaymentEventMessage event) {
        Message<PaymentEventMessage> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.KEY, event.getOrderId()) // 멱등성 키 지정
                .build();

        sink.tryEmitNext(message);
    }
}
