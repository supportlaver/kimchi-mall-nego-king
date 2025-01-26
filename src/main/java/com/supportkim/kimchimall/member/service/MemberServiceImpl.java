package com.supportkim.kimchimall.member.service;

import com.supportkim.kimchimall.cart.controller.port.CartService;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.jwt.JwtService;
import com.supportkim.kimchimall.member.controller.port.MemberService;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.supportkim.kimchimall.member.controller.request.MemberRequestDto.*;
import static com.supportkim.kimchimall.member.controller.response.MemberResponseDto.*;

@Service
@RequiredArgsConstructor
@Builder @Slf4j
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    @Override
    public MemberJoinResponse join(MemberJoinRequest request) {
        String encodePassword = passwordEncoder.encode(request.getPassword());
        Member member = memberRepository.save(MemberJoinRequest.toModel(request,encodePassword));
        return MemberJoinResponse.from(member);
    }

    @Override
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_PK_ID_NOT_FOUND));
    }


    @Override
    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_LOGIN_ID_NOT_FOUND));
    }

    @Override
    public Member findByName(String name) {
        return memberRepository.findByName(name).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    }

    @Override
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_EMAIL_NOT_FOUND));
    }
}
