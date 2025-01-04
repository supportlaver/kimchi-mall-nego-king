package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration @Slf4j
@RequiredArgsConstructor
public class WalletPaymentEventHandler {
    private final WalletServiceForKafka walletServiceForKafka;
    @Bean
    public Consumer<PaymentEventMessage> wallet() {
        return walletServiceForKafka::processWalletEvent;
    }
}
