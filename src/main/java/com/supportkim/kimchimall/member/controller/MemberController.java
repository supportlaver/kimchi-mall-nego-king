package com.supportkim.kimchimall.member.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.member.controller.port.MemberService;
import com.supportkim.kimchimall.member.controller.request.MemberRequestDto;
import com.supportkim.kimchimall.member.controller.response.MemberResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.supportkim.kimchimall.member.controller.request.MemberRequestDto.*;
import static com.supportkim.kimchimall.member.controller.response.MemberResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<BaseResponse<MemberJoinResponse>> join(@RequestBody MemberJoinRequest memberJoinRequest) {
        return ResponseEntity.ok().body(new BaseResponse<>(memberService.join(memberJoinRequest)));
    }

}
