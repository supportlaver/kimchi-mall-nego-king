package com.supportkim.kimchimall.kimchi.service;

import com.supportkim.kimchimall.cart.domain.Cart;
import com.supportkim.kimchimall.cart.service.port.CartRepository;
import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.common.exception.NaverShoppingApiConnectionRefusedException;
import com.supportkim.kimchimall.common.security.jwt.JwtService;
import com.supportkim.kimchimall.kimchi.controller.port.FindLowestPriceService;
import com.supportkim.kimchimall.kimchi.controller.port.KimchiService;
import com.supportkim.kimchimall.kimchi.controller.request.KimchiRequestDto;
import com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto;
import com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiCacheRepository;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.kimchi.infrastructure.LowestPriceKimchiCacheRepository;
import com.supportkim.kimchimall.kimchi.service.port.KimchiRepository;
import com.supportkim.kimchimall.member.controller.port.MemberService;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.service.MemberServiceImpl;
import com.supportkim.kimchimall.member.service.port.MemberRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.supportkim.kimchimall.kimchi.controller.request.KimchiRequestDto.*;
import static com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto.*;
import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;
import static feign.FeignException.*;

@Service
@RequiredArgsConstructor
@Slf4j @Builder
public class KimchiServiceImpl implements KimchiService {

    private final FindLowestPriceService findLowestPriceService;
    private static final String NAVER_OPEN_API_CIRCUIT_BREAKER_CONFIG = "naverOpenApiCircuitBreakerConfig";
    private final LowestPriceKimchiCacheRepository lowestPriceKimchiCacheRepository;
    private final KimchiRepository kimchiRepository;
    private final CartRepository cartRepository;
    private final JwtService jwtService;
    private final MemberService memberService;
    private final KimchiCacheRepository kimchiCacheRepository;

    @Override
    public void putKimchiInCart(PutCart putCartDto , HttpServletRequest request) {

        Member member = findMemberFromAccessToken(request);
        Cart memberCart = member.getCart();
    }

    @Override
    public SingleKimchi getKimchi(String kimchiName) {
        Optional<Kimchi> cacheKimchi = kimchiCacheRepository.getKimchi(kimchiName);
        // Cache 에서 우선 확인하고 없다면 그때 DB 에서 조회
        if (cacheKimchi.isPresent()) {
            return SingleKimchi.from(cacheKimchi.get());
        }
        return SingleKimchi.from(kimchiRepository.findByName(kimchiName));
    }

    @Override
    public List<KimchiEntity> getKimchis(List<Long> kimchiIds) {
        return kimchiRepository.findByIds(kimchiIds);
    }

    private Member findMemberFromAccessToken(HttpServletRequest request) {
        String accessToken = jwtService.extractAccessToken(request)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_EXIST_TOKEN));
        String email = jwtService.extractMemberEmail(accessToken);
        return memberService.findByEmail(email);
    }

    @Override
    @CircuitBreaker(name = NAVER_OPEN_API_CIRCUIT_BREAKER_CONFIG , fallbackMethod = "fallback")
    public FindLowestPriceResponseDto getFindLowestPrice(String type , String sort , int display , int start) {
        return findLowestPriceService.getFindLowestPriceKimchis(type, sort, display, start);
    }

    /**
     * fallback Method 는 CircuitBreaker 에서 지정한 메서드의 시그니처가 동일해야한다.
     * 반환값 , 파라미터가 동일해야하며 추가로 Exception 인자까지 가지고 있어야한다.
     */
    public FindLowestPriceResponseDto fallback(String type , String sort , int display , int start, NaverShoppingApiConnectionRefusedException ex) {
        log.info("네이버 API 에러 -> 캐시된 데이터로 반환");
        // List<ItemDto> cacheList = lowestPriceKimchiCacheRepository.getLowestPriceKimchiCache(type);
        List<ItemDto> cacheList = lowestPriceKimchiCacheRepository.getLowestPriceKimchiCache("B");
        // 로그는 DB 에 저장 예정
        log.info("fallback-> CallNotPermittedException : {}" , ex.getMessage());
        log.info("fallback-> CallNotPermittedException : {}" , ex.getStackTrace());
        return FindLowestPriceResponseDto.from(cacheList, display, start);
    }
    public FindLowestPriceResponseDto fallback(String type , String sort , int display , int start,CallNotPermittedException ex) {
        log.info("현재 Naver API 에 문제가 있습니다. 잠시후에 다시 시도해주세요.");
        // 캐시된 상품을 노출
        List<ItemDto> cacheList = lowestPriceKimchiCacheRepository.getLowestPriceKimchiCache(type);

        log.info("fallback-> CallNotPermittedException : {}" , ex.getMessage());
        log.info("fallback-> CallNotPermittedException : {}" , ex.getStackTrace());

        return FindLowestPriceResponseDto.from(cacheList, display, start);
    }

    public FindLowestPriceResponseDto fallback(String type , String sort , int display , int start,TooManyRequests ex) {
        log.info("현재 사용자가 많아 요청 시간이 오래 걸리고 있습니다. 잠시후에 다시 시도해주세요");
        // 캐시한 상품을 노출
        List<ItemDto> cacheList = lowestPriceKimchiCacheRepository.getLowestPriceKimchiCache(type);

        // 로그는 DB 에 저장 예정
        log.info("fallback-> NaverApiException : {}" , ex.getMessage());
        log.info("fallback-> NaverAPiException : {}" , ex.getStackTrace());

        // Cache 된 상품을 반환한다.
        return FindLowestPriceResponseDto.from(cacheList, display, start);
    }

    @Override
    public Kimchis getKimchis() {
        return kimchiRepository.getKimchis();
    }




}
