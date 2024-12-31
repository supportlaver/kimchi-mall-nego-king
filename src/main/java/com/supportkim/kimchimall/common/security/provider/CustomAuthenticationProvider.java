package com.supportkim.kimchimall.common.security.provider;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.security.service.CustomMemberDetailsService;
import com.supportkim.kimchimall.common.security.service.MemberContext;
import com.supportkim.kimchimall.common.security.token.CustomAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomMemberDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        // 1차 아이디 확인
        MemberContext memberContext = (MemberContext) userDetailsService.loadUserByUsername(loginId);

        // 2차 비밀번호 확인
        if (!passwordEncoder.matches(password,memberContext.getMember().getPassword())){
            throw new BaseException(ErrorCode.MEMBER_PW_NOT_FOUND);
        }

        return new CustomAuthenticationToken(memberContext.getMember() , null , memberContext.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(CustomAuthenticationToken.class);
    }
}
