package com.supportkim.kimchimall.ledger.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.ledger.infrasturcture.event.LedgerCompleteEventMessage;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import com.supportkim.kimchimall.member.infrastructure.MemberJpaRepository;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrderJpaRepository;
import com.supportkim.kimchimall.payment.service.dto.DoubleAccountsForLedger;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.Wallet;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletJpaRepository;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransaction;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransactionJpaRepository;
import com.supportkim.kimchimall.wallet.infrasturcture.event.WalletCompleteEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration @Slf4j
@RequiredArgsConstructor
public class LedgerPaymentEventHandler {

    private final LedgerService ledgerService;
    @Bean
    public Consumer<PaymentEventMessage> ledger() {
        return ledgerService::ledger;
    }
}
