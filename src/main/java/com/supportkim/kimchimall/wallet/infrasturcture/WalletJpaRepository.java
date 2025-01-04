package com.supportkim.kimchimall.wallet.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface WalletJpaRepository extends JpaRepository<Wallet , Long> {
    @Query("SELECT w FROM Wallet w WHERE w.userId IN :userIds")
    List<Wallet> findByUserIds(@Param("userIds") Set<Long> userIds);
}
