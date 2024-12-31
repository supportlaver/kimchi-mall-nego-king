package com.supportkim.kimchimall.common.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenMapping {
    private String accessToken;
    private String refreshToken;

    @Builder
    public TokenMapping(String accessToken , String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
