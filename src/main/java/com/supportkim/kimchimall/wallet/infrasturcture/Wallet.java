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

    public List<WalletTransaction> calculateBalanceWith(List<PaymentOrder> paymentOrders) {
        BigDecimal totalAmount = paymentOrders.stream()
                .map(PaymentOrder::getAmount)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WalletTransaction> newTransactions = paymentOrders.stream()
                .map(paymentOrder -> WalletTransaction.createWalletTransaction(
                        this.id,
                        BigDecimal.valueOf(paymentOrder.getAmount()),
                        TransactionType.CREDIT,
                        "referenceId",
                        1L,
                        paymentOrder.getOrderId()
                ))
                .toList();

        this.balance = this.balance.add(totalAmount);
        return newTransactions;
    }
}
