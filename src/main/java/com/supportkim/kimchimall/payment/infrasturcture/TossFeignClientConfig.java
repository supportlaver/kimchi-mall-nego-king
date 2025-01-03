package com.supportkim.kimchimall.payment.infrasturcture;

import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class TossFeignClientConfig {

    @Value("${PSP.toss.secretKey}")
    private String secretKey;

    // 요청 시 공통 헤더 설정 (Basic Auth + JSON)
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Basic {secretKey:} 형태
            String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            String authorizationHeader = "Basic " + encodedKey;

            requestTemplate.header("Authorization", authorizationHeader);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    // Feign 타임아웃 설정
    @Bean
    public Request.Options requestOptions() {
        // connectTimeoutMillis, readTimeoutMillis
        return new Request.Options(30_000, 30_000);
    }

}
