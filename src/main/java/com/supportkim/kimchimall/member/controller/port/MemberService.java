package com.supportkim.kimchimall.member.controller.port;

import com.supportkim.kimchimall.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;

import static com.supportkim.kimchimall.member.controller.request.MemberRequestDto.*;
import static com.supportkim.kimchimall.member.controller.response.MemberResponseDto.*;

public interface MemberService {
    MemberJoinResponse join(MemberJoinRequest memberJoinRequestDto);
    Member findById(Long id);
    MemberLoginResponse login(MemberLoginRequest memberLoginRequestDto);
    Member findByLoginId(String loginId);
    Member findByName(String name);
    Member findByEmail(String email);
    Member findByAccessToken(HttpServletRequest request);
}
