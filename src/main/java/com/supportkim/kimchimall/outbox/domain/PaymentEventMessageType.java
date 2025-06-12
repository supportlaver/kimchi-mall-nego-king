package com.supportkim.kimchimall.outbox.domain;

public enum PaymentEventMessageType {
    PAYMENT_CONFIRMATION_SUCCESS("결제 승인 완료"),
    PAYMENT_CANCELED("결제 취소"),
    WALLET_UPDATED("지갑 반영"),
    LEDGER_UPDATED("장부 반영");

    private final String description;

    PaymentEventMessageType(String description) {
        this.description = description;
    }
}
