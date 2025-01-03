package com.supportkim.kimchimall.kimchi.service.port;

import com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;

import java.util.List;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

public interface KimchiRepository {
    Kimchis getKimchis();
    Kimchi findById(Long kimchiId);
    Kimchi findByName(String kimchiName);
    void save(Kimchi kimchi);

    // TODO: 1/3/25 Entity 와 Domain 분리
    List<KimchiEntity> findByIds(List<Long> kimchiIds);
}
