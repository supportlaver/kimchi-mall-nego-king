package com.supportkim.kimchimall.outbox.domain;

import com.supportkim.kimchimall.common.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Outbox extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.INIT;

    @Column(length = 40)
    private String type;

    @Column(name = "partition_key", nullable = false)
    private Integer partitionKey = 0;

    @Lob
    @Column(columnDefinition = "json")
    private String payload;

    @Lob
    @Column(columnDefinition = "json")
    private String metadata;

    public static Outbox of(String idempotencyKey,
                            OutboxStatus status,
                            String type,
                            Integer partitionKey,
                            String payload,
                            String metadata) {
        Outbox outbox = new Outbox();
        outbox.idempotencyKey = idempotencyKey;
        outbox.status = status;
        outbox.type = type;
        outbox.partitionKey = partitionKey;
        outbox.payload = payload;
        outbox.metadata = metadata;
        return outbox;
    }


    public void updateStatus(OutboxStatus outboxStatus) {
        this.status = outboxStatus;
    }
}
