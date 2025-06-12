package com.supportkim.kimchimall.member.controller.request;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
public class MemberRequestDto {
    @Builder @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberJoinRequest {
        private String name;
        private String phoneNumber;
        private Address address;
        private String email;
        private String loginId;
        private String password;

        public static Member toModel(MemberJoinRequest request , String encodePassword) {
            return Member.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .loginId(request.getLoginId())
                    .password(encodePassword)
                    .orders(new ArrayList<>())
                    .build();
        }
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class MemberLoginRequest {
        private String loginId;
        private String password;
    }
}
