package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration @Slf4j
@RequiredArgsConstructor
public class WalletPaymentEventHandler {
    private final WalletServiceForKafka walletServiceForKafka;


    @Bean
    public Consumer<Message<PaymentEventMessage>> wallet() {
        return message -> {
            PaymentEventMessage payload = message.getPayload();
            walletServiceForKafka.processWalletEvent(payload);
        };
    }
}
