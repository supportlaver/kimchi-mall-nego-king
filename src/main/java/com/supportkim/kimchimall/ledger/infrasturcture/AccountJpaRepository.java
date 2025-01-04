package com.supportkim.kimchimall.ledger.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountJpaRepository extends JpaRepository<Account , Long> {
    @Query("SELECT a FROM Account a WHERE a.member.id = :memberId")
    Optional<Account> findByMemberId(@Param("memberId") Long memberId);
    @Query("SELECT a FROM Account a WHERE a.member.id IN :memberIds")
    List<Account> findByMemberIds(@Param("memberIds") Set<Long> memberIds);

}
