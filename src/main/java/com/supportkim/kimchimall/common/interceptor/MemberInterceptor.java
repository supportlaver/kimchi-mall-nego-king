package com.supportkim.kimchimall.common.interceptor;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.jwt.JwtService;
import com.supportkim.kimchimall.member.controller.response.MemberResponseDto;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.MemberCacheRepository;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * 이부분은 스프링 AOP 를 통해 구현하는 것도 좋은 방법 (추후에 해보기)
 */
@Component @Slf4j
@RequiredArgsConstructor
public class MemberInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final MemberCacheRepository cacheRepository;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        log.info("requestUrl = {} " , requestUrl);
        if (requestUrl.startsWith("/test")) {
            return true;
        }

        if (requestUrl.startsWith("/success")) {
            return true;
        }

        if (requestUrl.startsWith("/fail")) {
            return true;
        }

        if (requestUrl.startsWith("/api")) {
            return true;
        }
        String accessToken = jwtService.extractAccessToken(request)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_EXIST_TOKEN));
        String email = jwtService.extractMemberEmail(accessToken);

        Optional<Member> optionalMember = cacheRepository.getMember(email);
        Member member = optionalMember.orElseGet(() ->
                memberRepository.findByEmailWithCartAndCartKimchi(email)
                        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_EMAIL_NOT_FOUND))
        );

        request.setAttribute("member", member);
        return true;
    }
}
