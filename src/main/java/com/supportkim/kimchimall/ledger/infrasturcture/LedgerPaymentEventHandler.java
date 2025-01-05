package com.supportkim.kimchimall.ledger.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration @Slf4j
@RequiredArgsConstructor
public class LedgerPaymentEventHandler {

    private final LedgerServiceForKafka ledgerServiceForKafka;
    @Bean
    public Consumer<PaymentEventMessage> ledger() {
        return ledgerServiceForKafka::ledger;
    }
}
