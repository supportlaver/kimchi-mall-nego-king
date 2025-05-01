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
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LedgerServiceForKafka {

    private final StreamBridge streamBridge;
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final LedgerTransactionJpaRepository ledgerTransactionRepository;
    private final AccountJpaRepository accountRepository;
    private final LedgerEntryJpaRepository ledgerEntryRepository;
    private final MemberJpaRepository memberRepository;

    @Transactional
    public void ledger(PaymentEventMessage event) {
        System.out.println("메시지 소비 - Ledger");
        if (ledgerTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_LEDGER_PROCESS);
        }

        MemberEntity buyer = memberRepository.findById(event.getBuyerId()).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
        Account buyerAccount = accountRepository.findByMemberId(buyer.getId()).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_ACCOUNT_NOT_FOUND));

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());

        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));

        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();
        List<Account> sellerAccounts = accountRepository.findByMemberIds(sellerIds);
        List<DoubleAccountsForLedger> ledgerList = sellerAccounts.stream()
                .map(sellerAccount -> {
                    DoubleAccountsForLedger ledger = new DoubleAccountsForLedger();
                    ledger.setTo(buyerAccount);    // 구매자 계좌
                    ledger.setFrom(sellerAccount); // 판매자 계좌
                    return ledger;
                })
                .toList();

        List<LedgerEntry> ledgerEntries = createLedgerEntries(ledgerList, paymentOrders);
        ledgerEntryRepository.saveAll(ledgerEntries);
        // 업데이트
        paymentOrders.forEach(PaymentOrder::confirmLedgerUpdate);

        streamBridge.send("ledger-result", new LedgerCompleteEventMessage(event.getOrderId()));
    }
    private List<LedgerEntry> createLedgerEntries(List<DoubleAccountsForLedger> ledgerList, List<PaymentOrder> paymentOrders) {
        return ledgerList.stream()

                .flatMap(ledger -> paymentOrders.stream().flatMap(order -> {
                    LedgerTransaction transaction = LedgerTransaction.builder()
                            .referenceType("PAYMENT_ORDER")
                            .referenceId(1L)
                            .orderId(order.getOrderId())
                            .idempotencyKey(order.getOrderId())
                            .build();

                    LedgerEntry creditEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getTo().getId())
                            .transaction(transaction)
                            .type(LedgerEntryType.CREDIT)
                            .build();

                    LedgerEntry debitEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getFrom().getId())
                            .transaction(transaction)
                            .type(LedgerEntryType.DEBIT)
                            .build();

                    return Stream.of(creditEntry, debitEntry);
                }))
                .collect(Collectors.toList());
    }



}
