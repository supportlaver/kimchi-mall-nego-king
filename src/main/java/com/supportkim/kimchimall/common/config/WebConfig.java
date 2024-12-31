package com.supportkim.kimchimall.common.config;

import com.supportkim.kimchimall.common.interceptor.MemberInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MemberInterceptor memberInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",        // actuator 경로 제외
                        "/api/join",           // 회원가입 경로 제외
                        "/api/login",          // 로그인 경로 제외
                        "/actuator/prometheus", // Prometheus 경로 제외
                        "/error"               // 에러 경로 제외
                );
    }

}