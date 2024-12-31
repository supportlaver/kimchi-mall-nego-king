package com.supportkim.kimchimall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public CustomRequestInterceptor feignInterceptor() {
        return new CustomRequestInterceptor();
    }
}
