package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.ledger.infrasturcture.event.LedgerCompleteEventMessage;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrderJpaRepository;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.event.WalletCompleteEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service @Slf4j
@RequiredArgsConstructor
public class WalletServiceForKafka {

    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final WalletJpaRepository walletRepository;
    private final StreamBridge streamBridge;
    @Transactional
    public void processWalletEvent(PaymentEventMessage event) {
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

        streamBridge.send("wallet-result", new LedgerCompleteEventMessage(event.getOrderId()));
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
