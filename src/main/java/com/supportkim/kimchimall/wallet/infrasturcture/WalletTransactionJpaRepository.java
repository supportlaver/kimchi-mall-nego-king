package com.supportkim.kimchimall.wallet.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction , Long> {
    Boolean existsByOrderId(String orderId);
}
