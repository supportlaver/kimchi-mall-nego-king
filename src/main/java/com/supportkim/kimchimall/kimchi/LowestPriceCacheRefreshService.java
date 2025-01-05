package com.supportkim.kimchimall.kimchi;

import com.supportkim.kimchimall.kimchi.controller.port.FindLowestPriceService;
import com.supportkim.kimchimall.kimchi.controller.port.KimchiService;
import com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto;
import com.supportkim.kimchimall.kimchi.domain.KimchiType;
import com.supportkim.kimchimall.kimchi.infrastructure.LowestPriceKimchiCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service @Slf4j
@RequiredArgsConstructor
public class LowestPriceCacheRefreshService {

    private final LowestPriceKimchiCacheRepository lowestPriceKimchiCacheRepository;
    private final KimchiService kimchiService;

    @Scheduled(fixedRate = 6000000)
    public void refreshKimchiLowestPriceCache() {
        lowestPriceKimchiCacheRepository.clearAll();
        KimchiType[] values = KimchiType.values();
        for (KimchiType value : values) {
            log.info("refreshKimchiLowestPriceCache 실행 = {} " , value.getName());
            FindLowestPriceResponseDto findLowestPriceKimchis = kimchiService.getFindLowestPrice(value.getName(), "asc", 10, 1);
            lowestPriceKimchiCacheRepository.setLowestPriceKimchiCache(findLowestPriceKimchis.getItems() , value.name());
        }
        log.info("스케줄링 끝난 후 데이터 확인");

        for (KimchiType value : values) {
            List<FindLowestPriceResponseDto.ItemDto> lowestPriceKimchiCache = lowestPriceKimchiCacheRepository.getLowestPriceKimchiCache(value.toString());
            log.info("확인할 김치 종류  = {} " , value.getName());
            for (FindLowestPriceResponseDto.ItemDto itemDto : lowestPriceKimchiCache) {
                log.info("kimchi = {} " , itemDto.getHprice());
                log.info("kimchi = {} " , itemDto.getMallName());
                log.info("kimchi = {} " , itemDto.getLink());
            }
        }
    }

}
