package com.supportkim.kimchimall.fail;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JobFailureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobType;       // 예: "정산처리", "장부기입"
    private String orderId;       // 어떤 주문에서 실패했는지
    private String reason;        // "정산 처리 중 오류" 등
    private String errorMessage;  // 구체적인 예외 메시지

    private LocalDateTime occurredAt;

    public static JobFailureLog of(String jobType, String orderId, String reason, String errorMessage) {
        return JobFailureLog.builder()
                .jobType(jobType)
                .orderId(orderId)
                .reason(reason)
                .errorMessage(errorMessage)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}