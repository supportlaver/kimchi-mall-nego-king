package com.supportkim.kimchimall.ledger.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntry , Long> {
}
