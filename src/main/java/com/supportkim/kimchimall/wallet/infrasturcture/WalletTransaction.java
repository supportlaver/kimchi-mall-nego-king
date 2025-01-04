package com.supportkim.kimchimall.wallet.infrasturcture;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Entity @Getter @Builder
@Table(name = "wallet_transactions")
@NoArgsConstructor @AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id")
    private Long walletId;
    @Column
    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    private TransactionType type;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;


    public static WalletTransaction createWalletTransaction(Long walletId , BigDecimal amount , TransactionType type , String referenceType , Long referenceId , String orderId) {
        return WalletTransaction.builder()
                .walletId(walletId)
                .amount(amount)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .orderId(orderId)
                .build();
    }



}
