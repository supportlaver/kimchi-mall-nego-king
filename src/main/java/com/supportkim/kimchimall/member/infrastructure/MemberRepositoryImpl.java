package com.supportkim.kimchimall.member.infrastructure;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository @Slf4j
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(MemberEntity.fromForJoin(member)).toModel();
    }

    @Override
    public Member saveForLogin(Member member) {
        return memberJpaRepository.save(MemberEntity.fromForLogin(member)).toModelForLogin();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(memberJpaRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND))
                .toModel());
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return Optional.ofNullable(memberJpaRepository.findByLoginId(loginId).toModel());
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return Optional.ofNullable(memberJpaRepository.findByEmail(email).toModelForLogin());
    }

    @Override
    public Optional<Member> findByEmailWithCartAndCartKimchi(String email) {
        MemberEntity member = memberJpaRepository.findByEmailWithCart(email);
        return Optional.ofNullable(member.toModelForLogin());
    }

    @Override
    public Optional<Member> findByName(String name) {
        return Optional.ofNullable(memberJpaRepository.findByName(name).toModel());
    }

    @Override
    public void deleteAll() {
        memberJpaRepository.deleteAll();
    }

    @Override
    public void flush() {
        memberJpaRepository.flush();
    }

}
