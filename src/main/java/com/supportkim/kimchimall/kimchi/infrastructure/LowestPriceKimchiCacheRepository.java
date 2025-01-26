package com.supportkim.kimchimall.kimchi.infrastructure;

import com.supportkim.kimchimall.kimchi.domain.KimchiType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto.*;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LowestPriceKimchiCacheRepository {

    private final RedisTemplate<String, ItemDto> lowestPriceKimchiRedisTemplate;
    private static final String KIMCHI_HASH_KEY = "kimchi";
    private static final Duration KIMCHI_CACHE_TTL = Duration.ofDays(3);

    public void setLowestPriceKimchiCache(List<ItemDto> kimchis, String type) {
        kimchis.forEach(item -> {
            lowestPriceKimchiRedisTemplate.opsForList().rightPush(type, item);
        });
    }

    public List<ItemDto> getLowestPriceKimchiCache(String type) {
        List<ItemDto> items = lowestPriceKimchiRedisTemplate.opsForList().range(type, 0, 9);
        return items;
    }

    public void clear(String type) {
        lowestPriceKimchiRedisTemplate.delete(type);
    }

    public void clearAll() {
        KimchiType[] types = KimchiType.values();
        for (KimchiType type : types) {
            clear(type.name());
        }
        log.info("모든 김치 캐시 데이터가 삭제되었습니다.");
    }
}
