package com.supportkim.kimchimall.payment.infrasturcture;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_order")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_event_id", nullable = false)
    private PaymentEvent paymentEvent;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long kimchiId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private boolean isLedgerUpdated = false;

    @Column(nullable = false)
    private boolean isWalletUpdated = false;

    public static PaymentOrder of(Long sellerId, String idempotencyKey, Long kimchiId, int price, PaymentStatus status) {
        return PaymentOrder.builder()
                .sellerId(sellerId)
                .orderId(idempotencyKey)
                .kimchiId(kimchiId)
                .amount(price)
                .paymentStatus(status)
                .build();
    }


    public boolean isLedgerUpdated() {
        return isLedgerUpdated;
    }

    public boolean isWalletUpdated() {
        return isWalletUpdated;
    }

    public void confirmWalletUpdate() {
        this.isWalletUpdated = true;
    }

    public void confirmLedgerUpdate() {
        this.isLedgerUpdated = true;
    }

    // 연관관계 편의 메서드
    public void settingPaymentEvent(PaymentEvent paymentEvent) {
        this.paymentEvent = paymentEvent;
    }
}
