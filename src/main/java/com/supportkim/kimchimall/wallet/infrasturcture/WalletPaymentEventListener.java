package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.fail.JobFailureLog;
import com.supportkim.kimchimall.fail.JobFailureLogJpaRepository;
import com.supportkim.kimchimall.ledger.infrasturcture.AccountJpaRepository;
import com.supportkim.kimchimall.ledger.infrasturcture.LedgerEntryJpaRepository;
import com.supportkim.kimchimall.ledger.infrasturcture.LedgerTransactionJpaRepository;
import com.supportkim.kimchimall.member.infrastructure.MemberJpaRepository;
import com.supportkim.kimchimall.payment.infrasturcture.*;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.event.WalletCompleteEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component @Slf4j
@RequiredArgsConstructor
@EnableAsync
public class WalletPaymentEventListener {
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final JobFailureLogJpaRepository jobFailureLogRepository;
    private final WalletJpaRepository walletRepository;

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            value = { DataAccessResourceFailureException.class , SocketTimeoutException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Async
    public void walletProcess(PaymentEventMessage event) {
        if (walletTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_WALLET_PROCESS);
        }

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());
        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));
        getUpdatedWallets(paymentOrdersBySellerId);
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        List<Wallet> wallets = walletRepository.findByUserIdsWithLock(sellerIds);

        wallets.forEach(wallet -> {
            List<WalletTransaction> transactions =
                    wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId()));
            walletTransactionRepository.saveAll(transactions);
        });

        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);

        eventPublisher.publishEvent(new WalletCompleteEventMessage(event.getOrderId()));
    }

    @Recover
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recover(Exception ex, PaymentEventMessage event) {
        log.error("정산 재시도 3회 실패 → 로그 저장. orderId={}", event.getOrderId(), ex);
        JobFailureLog log = JobFailureLog.of("SETTLEMENT" , event.getOrderId() , "SETTLEMENT_ERROR" , ex.getMessage());
        jobFailureLogRepository.save(log);
    }

    private void getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        List<Wallet> wallets = walletRepository.findByUserIdsWithLock(sellerIds);

        wallets.forEach(wallet -> {
            List<WalletTransaction> transactions =
                    wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId()));
            walletTransactionRepository.saveAll(transactions);
        });
    }
}
