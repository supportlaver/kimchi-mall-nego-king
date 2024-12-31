package com.supportkim.kimchimall.common.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    // 인증 전 생성자
    public CustomAuthenticationToken(Object principal , Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        // 인증 전
        setAuthenticated(false);
    }

    // 인증 후 생성자
    public CustomAuthenticationToken(Object principal , Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        // 인증 완료
        super.setAuthenticated(true);
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        super.setAuthenticated(authenticated);
    }


    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }


}
