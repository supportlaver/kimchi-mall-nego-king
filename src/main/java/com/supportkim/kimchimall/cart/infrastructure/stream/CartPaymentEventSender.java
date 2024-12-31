package com.supportkim.kimchimall.cart.infrastructure.stream;//package com.supportkim.kimchimall.cart.infrastructure.stream;
//
//
//import com.supportkim.kimchimall.common.util.PartitionKeyUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.IntegrationMessageHeaderAccessor;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.channel.FluxMessageChannel;
//import org.springframework.kafka.annotation.KafkaHandler;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Sinks;
//import reactor.kafka.sender.SenderResult;
//
//import java.util.function.Supplier;
//
///**
// * CartPaymentEventMessage 가 어떻게 만들어져서 payload , metadata 가 채워지는지 확인해보고 계속 작성하기
// * 현재 CheckoutRequest 랑 혼동된 상태
// */
//// @Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class CartPaymentEventSender {
//
//    // outBox pattern 적용 전
//    private final Sinks.Many<Message<CartOrderEventMessage>> sender = Sinks.many().unicast().onBackpressureBuffer();
//    private final Sinks.Many<SenderResult<String>> sendResult =  Sinks.many().unicast().onBackpressureBuffer();
//
//    private final PartitionKeyUtil keyUtil;
//
//    @Bean
//    public Supplier<Flux<Message<CartOrderEventMessage>>> send() {
//        System.out.println("JIWON : CartPaymentEventSender.send");
//        return () -> sender.asFlux().onErrorContinue((err , obj) ->
//                log.error("sendEventMessage" , err.getMessage() != null ? err.getMessage() : "failed to send eventMessage",err)
//        );
//    }
//
//    @Bean(name = "cart-order-result")
//    public FluxMessageChannel sendResultChannel() {
//        System.out.println("JIWON : CartPaymentEventSender.sendResultChannel");
//        return new FluxMessageChannel();
//    }
//
//    @ServiceActivator(inputChannel = "cart-order-result")
//    public void receiveSendResult(SenderResult<String> results) {
//        System.out.println("JIWON : CartPaymentEventSender.receiveSendResult");
//        if (results.exception() != null) {
//            log.error("sendEventMessage" , results.exception() != null ?
//                    results.exception().getMessage(): "failed to send eventMessage",results.exception());
//        }
//
//        sendResult.emitNext(results , Sinks.EmitFailureHandler.FAIL_FAST);
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void dispatchAfterCommit(CartOrderEventMessage eventMessage) {
//        System.out.println("JIWON : CartPaymentEventSender.dispatchAfterCommit");
//        dispatch(eventMessage);
//    }
//
//    public void dispatch(CartOrderEventMessage eventMessage) {
//        System.out.println("JIWON : CartPaymentEventSender.dispatch");
//        sender.emitNext(createEventMessage(eventMessage) , Sinks.EmitFailureHandler.FAIL_FAST);
//    }
//
//    private Message<CartOrderEventMessage> createEventMessage(CartOrderEventMessage eventMessage) {
//        System.out.println("JIWON : CartPaymentEventSender.createEventMessage");
//        return MessageBuilder.withPayload(eventMessage)
//                .setHeader(KafkaHeaders.PARTITION , keyUtil.createPartitionKey(eventMessage.getIdempotencyKey().hashCode()))
//                .build();
//    }
//}
