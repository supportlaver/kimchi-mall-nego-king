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
    private Long id; // PK, BIG INT, AUTO INCREMENT

    @Column(name = "payment_order_id", nullable = false)
    private Long paymentOrderId; // FK, BIG INT, Payment Order 를 참조하는 식별자

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false)
    private PaymentStatus previousStatus; // ENUM, 변경 전 결제 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PaymentStatus newStatus; // ENUM, 변경 후 결제 상태

    @Column(name = "created_at")
    private LocalDateTime createdAt; // DATETIME, 생성된 시각

    @Column(name = "reason", length = 255)
    private String reason; // VARCHAR, 상태 변경의 이유


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
