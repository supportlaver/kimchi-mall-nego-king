package com.supportkim.kimchimall.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.common.security.jwt.JwtService;
import com.supportkim.kimchimall.common.security.jwt.TokenMapping;
import com.supportkim.kimchimall.common.util.SingletonObjectMapper;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.MemberCacheRepository;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.supportkim.kimchimall.member.controller.response.MemberResponseDto.*;

@Component @Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // ObjectMapper 싱글톤으로 사용하도록 하는게 더 좋아보인다.
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final MemberCacheRepository cacheRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Member member = (Member) authentication.getPrincipal();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        TokenMapping token = jwtService.createToken(member.getEmail());
        member.updateRefreshToken(token.getRefreshToken());

        // 로그인 성공 시 새로운 카트를 만들고 DB 에 저장
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);
        member.settingCart(savedCart);
        memberRepository.saveForLogin(member);
        // TODO: 9/9/24 프론트와 연결하게 되면 로컬 스토리지에 상품이 있다면 Cart 에 옮기는 작업이 필요합니다.
        log.info("member = {} " ,member);
        MemberLoginResponse loginResponse = MemberLoginResponse.from(member, token);
        // Cache 에 저장

        cacheRepository.setMember(member);

        ObjectMapper om = SingletonObjectMapper.getInstance();
        om.writeValue(response.getWriter() , loginResponse);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
