package com.supportkim.kimchimall.member.infrastructure;

import com.supportkim.kimchimall.member.controller.response.MemberResponseDto;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.SingleKimchi;
import static com.supportkim.kimchimall.member.controller.response.MemberResponseDto.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemberCacheRepository {
    private final RedisTemplate<String , Member> memberRedisTemplate;
    // TTL 걸어주기 (사용하지 않는 캐시 데이터 삭제)
    private final static Duration KIMCHI_CACHE_TTL = Duration.ofDays(3);

    public void setMember(Member member) {
        String key = getKey(member.getEmail());
        memberRedisTemplate.opsForValue().set(key,member,KIMCHI_CACHE_TTL);
    }

    public Optional<Member> getMember(String email) {
        Member member = memberRedisTemplate.opsForValue().get(getKey(email));
        return Optional.ofNullable(member);
    }

    public String

    getKey(String email) {
        return "MEMBER:" + email;
    }
}
