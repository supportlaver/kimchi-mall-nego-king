package com.supportkim.kimchimall.ledger.infrasturcture;

import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity @Getter
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private MemberEntity member;
}
