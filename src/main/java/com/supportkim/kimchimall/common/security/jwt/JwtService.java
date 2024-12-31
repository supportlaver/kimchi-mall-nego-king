package com.supportkim.kimchimall.common.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private final String PREFIX = "Bearer ";
    private final String BLANK = "";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidationSeconds;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidationSeconds;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    /**
     * TokenMapping 객체를 만들어서 반환
     */
    public TokenMapping createToken(String subject) {
        return TokenMapping.builder()
                .accessToken(createAccessToken(subject))
                .refreshToken(createRefreshToken())
                .build();
    }

    /**
     * 토큰 생성
     */

    public String createAccessToken(String subject) {
        return JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis()+accessTokenValidationSeconds))
                .withClaim("subject" , subject)
                .sign(Algorithm.HMAC512(secret));
    }

    public String createRefreshToken() {
        return JWT.create()
                .withSubject("RefreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis()+refreshTokenValidationSeconds))
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * 토큰을 헤더에 담아서 전송
     */

    public void setAccessTokenInHeader(HttpServletResponse response , String accessToken) {
        response.setHeader(accessHeader , accessToken);
    }

    public void setRefreshTokenInHeader(HttpServletResponse response , String refreshToken) {
        response.setHeader(accessHeader , refreshToken);
    }

    /**
     * 헤더에서 토큰 추출
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(PREFIX))
                .map(accessToken -> accessToken.replace(PREFIX , BLANK));
    }

    /**
     * extractAccessToken 과 같은 맥락이며, 여기서는 RefreshToken 을 추출합니다.
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(PREFIX))
                .map(refreshToken -> refreshToken.replace(PREFIX,BLANK));
    }

    /**
     * JWT 안에 있는 email 값을 추출할 수 있도록 한다.
     * JWT 를 email 로 만들었기 떄문에 디코딩 하게 되면 email 을 추출할 수 있다.
     */
    public String extractMemberEmail(String token) {
        return JWT.require(Algorithm.HMAC512(secret))
                .build()
                .verify(token.replace(PREFIX, BLANK))
                .getClaim("subject")
                .asString();
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            throw new BaseException(ErrorCode.TOKEN_NOT_VALID);
        }
    }
}
