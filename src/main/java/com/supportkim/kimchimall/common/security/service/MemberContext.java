package com.supportkim.kimchimall.common.security.service;

import com.supportkim.kimchimall.member.controller.MemberController;
import com.supportkim.kimchimall.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

/**
 * SpringSecurity 의 User 클래스를 확장하여 도메인 특정 사용자 정보를 추가하는 사용자 정의 클래스
 * Spring Security + JWT 인증 시스템을 위한 MemberContext
 */

public class MemberContext extends User {
    private final Member member;
    public MemberContext(Member member,Collection<? extends GrantedAuthority> authorities) {
        super(member.getLoginId(), member.getPassword(), authorities);
        this.member = member;
    }
    public Member getMember() {
        return member;
    }
}

