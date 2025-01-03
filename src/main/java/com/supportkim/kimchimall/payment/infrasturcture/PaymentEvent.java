package com.supportkim.kimchimall.payment.infrasturcture;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "payment_event")
@Getter
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column
    private LocalDateTime approvedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paymentEvent", orphanRemoval = true)
    private List<PaymentOrder> paymentOrders = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPaymentDone = false;

    public static PaymentEvent of(Long buyerId, String orderName, String orderId, List<PaymentOrder> paymentOrders) {
        return PaymentEvent.builder()
                .buyerId(buyerId)
                .orderName(orderName)
                .orderId(orderId)
                .paymentOrders(paymentOrders)
                .isPaymentDone(false)
                .build();
    }

    public int totalAmount() {
        return paymentOrders.stream()
                .mapToInt(PaymentOrder::getAmount)
                .sum();
    }

    public boolean isPaymentDone() {
        return isPaymentDone;
    }

    public boolean isSuccess() {
        return paymentOrders.stream()
                .allMatch(order -> order.getPaymentStatus() == PaymentStatus.SUCCESS);
    }

    public boolean isFailure() {
        return paymentOrders.stream()
                .allMatch(order -> order.getPaymentStatus() == PaymentStatus.FAILURE);
    }

    public boolean isUnknown() {
        return paymentOrders.stream()
                .allMatch(order -> order.getPaymentStatus() == PaymentStatus.UNKNOWN);
    }

    public void confirmWalletUpdate() {
        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);
    }

    public void confirmLedgerUpdate() {
        paymentOrders.forEach(PaymentOrder::confirmLedgerUpdate);
    }

    public void completeIfDone() {
        if (allPaymentOrdersDone()) {
            isPaymentDone = true;
        }
    }

    public boolean isLedgerUpdateDone() {
        return paymentOrders.stream()
                .allMatch(PaymentOrder::isLedgerUpdated);
    }

    public boolean isWalletUpdateDone() {
        return paymentOrders.stream()
                .allMatch(PaymentOrder::isWalletUpdated);
    }

    private boolean allPaymentOrdersDone() {
        return paymentOrders.stream()
                .allMatch(order -> order.isWalletUpdated() && order.isLedgerUpdated());
    }
}
