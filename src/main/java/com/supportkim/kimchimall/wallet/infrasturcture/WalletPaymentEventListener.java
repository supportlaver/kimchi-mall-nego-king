package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrderJpaRepository;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletPaymentEventListener {

    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final WalletJpaRepository walletRepository;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void walletProcess(PaymentEventMessage event) {

        if (walletTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_WALLET_PROCESS);
        }

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());

        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));
        getUpdatedWallets(paymentOrdersBySellerId);

        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);
    }

    private void getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        // 지갑 가져오기
        List<Wallet> wallets = walletRepository.findByUserIds(sellerIds);

        // 지갑 업데이트 후 WalletTransaction 저장
        wallets.forEach(wallet -> {
            // calculateBalanceWith 호출 후 반환된 WalletTransaction 저장
            List<WalletTransaction> transactions =
                    wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId()));

            // WalletTransaction 저장
            walletTransactionRepository.saveAll(transactions);
        });
    }
}
