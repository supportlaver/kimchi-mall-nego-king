package com.supportkim.kimchimall.kimchi.controller.port;

import com.supportkim.kimchimall.kimchi.controller.request.KimchiRequestDto;
import com.supportkim.kimchimall.kimchi.controller.response.FindLowestPriceResponseDto;
import com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.domain.KimchiType;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.supportkim.kimchimall.kimchi.controller.request.KimchiRequestDto.*;
import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

public interface KimchiService {
    FindLowestPriceResponseDto getFindLowestPrice(String type , String sort , int display, int start);
    Kimchis getKimchis();
    void putKimchiInCart(PutCart putCartDto , HttpServletRequest request);
    SingleKimchi getKimchi(String kimchiName);

    List<KimchiEntity> getKimchis(List<Long> kimchiIds);
}
