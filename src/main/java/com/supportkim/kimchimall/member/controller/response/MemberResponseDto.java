package com.supportkim.kimchimall.member.controller.response;

import com.supportkim.kimchimall.common.security.jwt.TokenMapping;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDto {

    @Builder @Getter
    public static class MemberJoinResponse {
        private Long id;
        private String name;

        public static MemberJoinResponse from(Member member) {
            return MemberJoinResponse.builder()
                    .id(member.getMemberId())
                    .name(member.getName())
                    .build();
        }
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class MemberLoginResponse {
        private Long memberId;
        private TokenMapping tokenMapping;
        private String email;
        public static MemberLoginResponse from(Member member , TokenMapping token) {
            return MemberLoginResponse.builder()
                    .memberId(member.getMemberId())
                    .email(member.getEmail())
                    .tokenMapping(token)
                    .build();
        }
    }
}
