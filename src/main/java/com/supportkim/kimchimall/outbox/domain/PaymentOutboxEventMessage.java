package com.supportkim.kimchimall.outbox.domain;

import java.util.Map;

public class PaymentOutboxEventMessage {
    private PaymentEventMessageType type;

    private Map<String, Object> payload;

    // 추가적인 메타데이터 (예: partitionKey, 발생 시각 등)
    private Map<String, Object> metadata;

    // 기본 생성자 (직렬화/역직렬화를 위해 필요)
    public PaymentOutboxEventMessage() {}

    public PaymentOutboxEventMessage(PaymentEventMessageType type,
                               Map<String, Object> payload,
                               Map<String, Object> metadata) {
        this.type = type;
        this.payload = payload;
        this.metadata = metadata;
    }

    // Getters & Setters
    public PaymentEventMessageType getType() {
        return type;
    }

    public void setType(PaymentEventMessageType type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
