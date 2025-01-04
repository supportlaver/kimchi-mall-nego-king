package com.supportkim.kimchimall.wallet.infrasturcture;

import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity @Getter
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal balance;

    // 낙관적 락 (동시성 이슈 해결)
    /*@Version
    private int version;*/


    /**
     * PaymentOrder 리스트를 이용해 Balance를 계산하고,
     * 해당 Transaction 목록을 갖는 새 Wallet 객체를 반환.
     */
    public List<WalletTransaction> calculateBalanceWith(List<PaymentOrder> paymentOrders) {
        // 1) PaymentOrder의 amount 합산
        BigDecimal totalAmount = paymentOrders.stream()
                .map(PaymentOrder::getAmount)
                .map(BigDecimal::valueOf)    // PaymentOrder.amount가 int라면 BigDecimal 변환
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2) WalletTransaction 생성
        List<WalletTransaction> newTransactions = paymentOrders.stream()
                .map(paymentOrder -> WalletTransaction.createWalletTransaction(
                        this.id,                          // walletId
                        BigDecimal.valueOf(paymentOrder.getAmount()),
                        TransactionType.CREDIT,
                        "referenceId",     // i() 대신 getReferenceId()
                        1L,
                        paymentOrder.getOrderId()
                ))
                .toList();

        // 3) 새 Wallet 객체 생성 및 반환 (기존 userId, version 그대로 사용)
        this.balance = this.balance.add(totalAmount);
        return newTransactions;
    }
}
