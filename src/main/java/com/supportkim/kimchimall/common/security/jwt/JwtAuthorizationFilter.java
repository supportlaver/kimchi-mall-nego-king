package com.supportkim.kimchimall.common.security.jwt;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.token.CustomAuthenticationToken;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = request.getRequestURI();

        if (requestUrl.startsWith("/test")) {
            log.info("Skipping JWT filter for request = {}", requestUrl);
            filterChain.doFilter(request, response);
            return;
        }

        if (requestUrl.startsWith("/success")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (requestUrl.startsWith("/fail")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresentOrElse(
                        this::saveAuthentication,
                        () -> checkRefreshToken(request,response)
                );
        filterChain.doFilter(request, response);
    }

    /**
     * accessToken 유효 -> authentication 저장
     * accessToken 만료
     *      refreshToken 유효 -> authentication 저장, accessToken 갱신
     *      refreshToken 만료 -> authentication 저장 X
     */


    private void saveAuthentication(String accessToken) {
        String email = jwtService.extractMemberEmail(accessToken);

        Member member = memberRepository.findByEmailWithCartAndCartKimchi(email)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_PK_ID_NOT_FOUND));

        CustomAuthenticationToken token = new CustomAuthenticationToken(member, null);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void checkRefreshToken(HttpServletRequest request , HttpServletResponse response) {
        Optional<String> refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid);

        if (refreshToken.isPresent()) {

        } else {
            doNotSaveAuthentication(request , response);
        }
    }

    private void doNotSaveAuthentication(HttpServletRequest request , HttpServletResponse response)  {

    }
}
