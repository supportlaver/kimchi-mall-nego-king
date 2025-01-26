package com.supportkim.kimchimall.kimchi.infrastructure;

import com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KimchiCacheRepository {
    private final RedisTemplate<String , Kimchi> kimchiRedisTemplate;
    private final static Duration KIMCHI_CACHE_TTL = Duration.ofDays(3);

    public void setKimchi(Kimchi kimchi) {
        String key = getKey(kimchi.getName());
        kimchiRedisTemplate.opsForValue().set(key,kimchi,KIMCHI_CACHE_TTL);
    }

    public Optional<Kimchi> getKimchi(String name) {
        Kimchi kimchi = kimchiRedisTemplate.opsForValue().get(getKey(name));
        return Optional.ofNullable(kimchi);
    }

    public void deleteKimchi(String name) {
        kimchiRedisTemplate.delete(getKey(name));
    }

    public String getKey(String name) {
        return "KIMCHI:" + name;
    }
}
