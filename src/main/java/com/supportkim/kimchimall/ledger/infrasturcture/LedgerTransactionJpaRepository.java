package com.supportkim.kimchimall.ledger.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionJpaRepository extends JpaRepository<LedgerTransaction , Long> {
}
