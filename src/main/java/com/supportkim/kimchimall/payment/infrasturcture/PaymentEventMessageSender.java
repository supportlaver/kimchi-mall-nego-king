package com.supportkim.kimchimall.payment.infrasturcture;


import com.supportkim.kimchimall.outbox.domain.OutboxStatus;
import com.supportkim.kimchimall.outbox.domain.PaymentEventMessageType;
import com.supportkim.kimchimall.outbox.domain.PaymentOutboxEventMessage;
import com.supportkim.kimchimall.outbox.service.OutboxService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.function.Supplier;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PaymentEventMessageSender {

    private final OutboxService outboxService;

    private final Sinks.Many<Message<PaymentOutboxEventMessage>> sender =
            Sinks.many().unicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<Message<PaymentOutboxEventMessage>>> outbox() {
        return () -> sender.asFlux()
                .onErrorContinue((err, obj) -> {
                    log.error("sendEventMessage failed: {}", err.getMessage(), err);
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dispatchAfterCommit(PaymentOutboxEventMessage paymentEventMessage) {
        dispatch(paymentEventMessage);
    }

    public void dispatch(PaymentOutboxEventMessage paymentEventMessage) {
        sender.emitNext(createEventMessage(paymentEventMessage), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private Message<PaymentOutboxEventMessage> createEventMessage(PaymentOutboxEventMessage paymentEventMessage) {
        return MessageBuilder.withPayload(paymentEventMessage)
                .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, paymentEventMessage.getPayload().get("orderId"))
                .setHeader(KafkaHeaders.PARTITION,
                        paymentEventMessage.getMetadata().getOrDefault("partitionKey", 0))
                .build();
    }
}
