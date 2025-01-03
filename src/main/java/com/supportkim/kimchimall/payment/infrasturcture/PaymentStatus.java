package com.supportkim.kimchimall.payment.infrasturcture;

import java.util.Arrays;

public enum PaymentStatus {
    NOT_STARTED("결제 시작 전"),
    EXECUTING("결제 중"),
    SUCCESS("결제 완료"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 승인 알 수 없는 상태");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    public static PaymentStatus get(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(paymentStatus -> paymentStatus.name().equals(status))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("PaymentStatus: " + status + " 는 올바르지 않은 결제 타입 입니다."));
    }
}
