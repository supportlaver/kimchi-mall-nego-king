package com.supportkim.kimchimall.ledger.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration @Slf4j
@RequiredArgsConstructor
public class LedgerPaymentEventHandler {

    private final LedgerServiceForKafka ledgerServiceForKafka;

    @Bean
    public Consumer<Message<PaymentEventMessage>> ledger() {
        return message -> {
            PaymentEventMessage payload = message.getPayload();
            ledgerServiceForKafka.ledger(payload);
        };
    }
}
