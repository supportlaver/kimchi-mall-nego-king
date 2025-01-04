package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrderJpaRepository;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.event.WalletCompleteEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceForKafka {

    private final PaymentOrderJpaRepository paymentOrderRepository;
    private final WalletTransactionJpaRepository walletTransactionRepository;
    private final WalletJpaRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    public void processWalletEvent(PaymentEventMessage event) {
        if (walletTransactionRepository.existsByOrderId(event.getOrderId())) {
            throw new BaseException(ErrorCode.ALREADY_PAYMENT_WALLET_PROCESS);
        }

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findByOrderId(event.getOrderId());
        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));
        // 5-4) 지갑 업데이트
        getUpdatedWallets(paymentOrdersBySellerId);
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

    private void getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> sellerIds = paymentOrdersBySellerId.keySet();

        // 지갑 가져오기
        List<Wallet> wallets = walletRepository.findByUserIdsWithLock(sellerIds);
        System.out.println("wallets = " + wallets.size());

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
