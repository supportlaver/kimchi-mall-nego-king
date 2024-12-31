package com.supportkim.kimchimall.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.token.CustomAuthenticationToken;
import com.supportkim.kimchimall.member.controller.request.MemberRequestDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.supportkim.kimchimall.member.controller.request.MemberRequestDto.*;

/**
 * AbstractAuthenticationProcessingFilter : 사용자 요청을 가로채고 인증을 수행
 * -> 특정 URL 패턴에 매핑되어 해당 URL 로 들어오는 요청을 가로채고 인증을 수행
 * -> 인증에 성공하면 : AuthenticationSuccessHandler
 * -> 인증에 실패하면 : AuthenticationFailureHandler
 */
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper om = new ObjectMapper();

    public CustomAuthenticationFilter() {
        super(new AntPathRequestMatcher("/api/login"));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // 요청 방식이 POST 인지 확인
        if (!isPost(request)) {
            throw new BaseException(ErrorCode.NOT_SUPPORT_METHOD);
        }

        MemberLoginRequest loginRequestMember = om.readValue(request.getReader(), MemberLoginRequest.class);

        if (!StringUtils.hasLength(loginRequestMember.getLoginId()) || !StringUtils.hasLength(loginRequestMember.getPassword())) {
            throw new BaseException(ErrorCode.EMPTY_LOGIN_INFO);
        }

        CustomAuthenticationToken token = new CustomAuthenticationToken(loginRequestMember.getLoginId() , loginRequestMember.getPassword());

        return getAuthenticationManager().authenticate(token);
    }

    private boolean isPost(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }
}
