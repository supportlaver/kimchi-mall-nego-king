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
import com.supportkim.kimchimall.wallet.infrasturcture.WalletJpaRepository;
import com.supportkim.kimchimall.wallet.infrasturcture.WalletTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class LedgerPaymentEventListener {

    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final LedgerTransactionJpaRepository ledgerTransactionRepository;
    private final AccountJpaRepository accountRepository;
    private final LedgerEntryJpaRepository ledgerEntryRepository;
    private final MemberJpaRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ledgerProcess(PaymentEventMessage event) {

        if (ledgerTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_LEDGER_PROCESS);
        }
        // 6-2) 계정 및 결제 주문 로드
        MemberEntity buyer = memberRepository.findById(event.getBuyerId()).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
        Account buyerAccount = accountRepository.findByMemberId(buyer.getId()).orElseThrow(()
                -> new BaseException(ErrorCode.MEMBER_ACCOUNT_NOT_FOUND));

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());

        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));

        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();
        List<Account> sellerAccounts = accountRepository.findByMemberIds(sellerIds);
        // 6-3) 복식부기 엔트리 생성 (Ledger)
        List<DoubleAccountsForLedger> ledgerList = sellerAccounts.stream()
                .map(sellerAccount -> {
                    DoubleAccountsForLedger ledger = new DoubleAccountsForLedger();
                    ledger.setTo(buyerAccount);    // 구매자 계좌
                    ledger.setFrom(sellerAccount); // 판매자 계좌
                    return ledger;
                })
                .toList();

        List<LedgerEntry> ledgerEntries = createLedgerEntries(ledgerList, paymentOrders);
        // 6-4) 복식 부기 엔트리 저장
        ledgerEntryRepository.saveAll(ledgerEntries);
        // 업데이트
        paymentOrders.forEach(PaymentOrder::confirmLedgerUpdate);
        eventPublisher.publishEvent(new LedgerCompleteEventMessage(event.getOrderId()));
    }
    private List<LedgerEntry> createLedgerEntries(List<DoubleAccountsForLedger> ledgerList, List<PaymentOrder> paymentOrders) {
        return ledgerList.stream()
                // ledger(구매자/판매자) x order(주문) 조합을 flatMap
                .flatMap(ledger -> paymentOrders.stream().flatMap(order -> {
                    LedgerTransaction transaction = LedgerTransaction.builder()
                            .referenceType("PAYMENT_ORDER")
                            .referenceId(1L)
                            .orderId(order.getOrderId())
                            .idempotencyKey(order.getOrderId())
                            .build();

                    LedgerEntry creditEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getTo().getId())  // buyer account
                            .transaction(transaction)
                            .type(LedgerEntryType.CREDIT)
                            .build();

                    LedgerEntry debitEntry = LedgerEntry.builder()
                            .amount(BigDecimal.valueOf(order.getAmount()))
                            .accountId(ledger.getFrom().getId())  // seller account
                            .transaction(transaction)
                            .type(LedgerEntryType.DEBIT)
                            .build();

                    // CREDIT/DEBIT 두 개의 엔티티를 Stream으로 반환
                    return Stream.of(creditEntry, debitEntry);
                }))
                .collect(Collectors.toList());
    }

}
