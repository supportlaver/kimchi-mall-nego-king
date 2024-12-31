package com.supportkim.kimchimall.order.controller.port;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * WebFlux 환경과 통신하기 위한 WebClientConfiguration
 */
// @Configuration
@RequiredArgsConstructor
public class CheckoutWebClientConfiguration {
    private static final String BASE_URL = "http://localhost:9000";
    /*@Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE , MediaType.APPLICATION_JSON_VALUE)
                .build();
    }*/
}
