package com.supportkim.kimchimall.ledger.infrasturcture;
import jakarta.persistence.*;
import lombok.Getter;

@Entity @Getter
@Table(name = "ledger_transactions")
public class LedgerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String description;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "reference_type", nullable = false)
    private String referenceType;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;
}
