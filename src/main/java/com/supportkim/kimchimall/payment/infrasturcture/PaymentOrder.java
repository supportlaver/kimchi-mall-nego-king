package com.supportkim.kimchimall.payment.infrasturcture;

import jakarta.persistence.*;
import lombok.Getter;


@Entity @Getter
@Table(name = "payment_order")
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentEventId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private boolean isLedgerUpdated = false;

    @Column(nullable = false)
    private boolean isWalletUpdated = false;
}
