package com.supportkim.kimchimall.common.security;

import com.supportkim.kimchimall.member.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/basic")
@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal Member member) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication.getName() = " + authentication.getName());
        System.out.println("member.getName() = " + member.getName());
        System.out.println("member.getEmail() = " + member.getEmail());
        System.out.println("member.getLoginId() = " + member.getLoginId());
        return "ok";
    }
}
