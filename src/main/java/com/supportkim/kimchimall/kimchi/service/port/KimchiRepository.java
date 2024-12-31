package com.supportkim.kimchimall.kimchi.service.port;

import com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;

import java.util.List;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

public interface KimchiRepository {
    Kimchis getKimchis();
    Kimchi findById(Long kimchiId);
    Kimchi findByName(String kimchiName);
    void save(Kimchi kimchi);
}
