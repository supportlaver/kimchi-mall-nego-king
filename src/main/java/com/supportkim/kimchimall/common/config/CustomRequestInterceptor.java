package com.supportkim.kimchimall.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * Naver Open API 사용
 * 모든 요청에 Header 추가를 위한 Interceptor
 */
public class CustomRequestInterceptor implements RequestInterceptor {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String secretKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Naver-Client-Id" , clientId);
        template.header("X-Naver-Client-Secret" , secretKey);
    }
}
