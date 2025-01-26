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

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            String authorizationHeader = "Basic " + encodedKey;

            requestTemplate.header("Authorization", authorizationHeader);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(30_000, 30_000);
    }

}
