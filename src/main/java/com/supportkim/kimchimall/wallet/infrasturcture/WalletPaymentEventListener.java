package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component @Slf4j
@RequiredArgsConstructor
public class WalletPaymentEventListener {
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final WalletJpaRepository walletRepository;

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // REQUIRES_NEW 는 TPS 가 높을 경우 너무 큰 부하를 갖게 됨
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void walletProcess(PaymentEventMessage event) {

        if (walletTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_WALLET_PROCESS);
        }

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());
        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));
        // 5-4) 지갑 업데이트
        // getUpdatedWallets(paymentOrdersBySellerId);
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        // 지갑 가져오기
        List<Wallet> wallets = walletRepository.findByUserIdsWithLock(sellerIds);


        // 지갑 업데이트 후 WalletTransaction 저장
        wallets.forEach(wallet -> {
            // calculateBalanceWith 호출 후 반환된 WalletTransaction 저장
            List<WalletTransaction> transactions =
                    wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId()));

            // WalletTransaction 저장
            walletTransactionRepository.saveAll(transactions);
        });

        // 지갑 업데이트가 성공적으로 끝났다면 Update
        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);

        eventPublisher.publishEvent(new WalletCompleteEventMessage(event.getOrderId()));
    }
}
