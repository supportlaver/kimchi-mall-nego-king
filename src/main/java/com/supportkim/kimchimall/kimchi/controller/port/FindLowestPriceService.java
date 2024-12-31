package com.supportkim.kimchimall.kimchi.controller.port;

import com.supportkim.kimchimall.common.config.FeignConfig;
import com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(name = "naver-open-api" , url = "https://openapi.naver.com/v1/search/shop.json",
        configuration = FeignConfig.class)
public interface FindLowestPriceService {
    @GetMapping
    FindLowestPriceResponseDto getFindLowestPriceKimchis(@RequestParam(value = "query") String query,
                                                         @RequestParam(value = "sort" , defaultValue = "asc") String sort ,
                                                         @RequestParam(value = "display" , defaultValue = "10") int display,
                                                         @RequestParam(value = "start" , defaultValue = "1") int start);
}
