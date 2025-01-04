package com.supportkim.kimchimall.ledger.infrasturcture;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity @Getter
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType type;
}
