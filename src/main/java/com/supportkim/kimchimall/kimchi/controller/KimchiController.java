package com.supportkim.kimchimall.kimchi.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.kimchi.controller.port.KimchiService;
import com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KimchiController {

    private final KimchiService kimchiService;
    private final KimchiCacheRepository redisCacheKimchiRepository;

    @GetMapping("/lowest-price")
    public ResponseEntity<BaseResponse<FindLowestPriceResponseDto>> getLowestPrice(
            @RequestParam("type") String type ,
            @RequestParam(value = "display" , defaultValue = "10") int display ,
            @RequestParam(value = "start" , defaultValue = "1") int start) {
        return ResponseEntity.ok().body(new BaseResponse<>(kimchiService.getFindLowestPrice(type,"asc",display,start)));
    }

    @GetMapping("/kimchis")
    public ResponseEntity<BaseResponse<Kimchis>> getKimchis() {
        return ResponseEntity.ok().body(new BaseResponse<>(kimchiService.getKimchis()));
    }

    @GetMapping("/kimchi")
    public ResponseEntity<BaseResponse<SingleKimchi>> getKimchi(@RequestParam("kimchi-name") String kimchiName) {
        return ResponseEntity.ok().body(new BaseResponse<>(kimchiService.getKimchi(kimchiName)));
    }
}
