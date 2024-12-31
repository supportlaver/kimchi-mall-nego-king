package com.supportkim.kimchimall.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberJpaRepository extends JpaRepository<MemberEntity , Long> {
    MemberEntity findByLoginId(String loginId);
    MemberEntity findByEmail(String email);
    MemberEntity findByName(String name);

    @Query("SELECT m FROM MemberEntity m LEFT JOIN FETCH m.cart c LEFT JOIN FETCH c.cartKimchiEntityList WHERE m.email = :email")
    MemberEntity findByEmailWithCart(@Param("email") String email);
}
