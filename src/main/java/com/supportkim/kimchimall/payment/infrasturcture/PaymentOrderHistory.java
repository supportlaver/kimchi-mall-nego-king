package com.supportkim.kimchimall.payment.infrasturcture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_order_history")
public class PaymentOrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_order_id", nullable = false)
    private Long paymentOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false)
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PaymentStatus newStatus;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reason", length = 255)
    private String reason;


    public static PaymentOrderHistory of(
            Long paymentOrderId,
            PaymentStatus previousStatus,
            PaymentStatus newStatus,
            String reason
    ) {
        return PaymentOrderHistory.builder()
                .paymentOrderId(paymentOrderId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .createdAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }
}
