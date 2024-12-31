package com.supportkim.kimchimall.kimchi.infrastructure;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.service.port.KimchiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.supportkim.kimchimall.kimchi.controller.response.KimchiResponseDto.*;

@Repository @Slf4j
@RequiredArgsConstructor
public class KimchiRepositoryImpl implements KimchiRepository {

    private final KimchiJpaRepository kimchiJpaRepository;
    @Override
    public Kimchis getKimchis() {
        List<KimchiEntity> kimchiEntities = kimchiJpaRepository.findAll();
        List<SingleKimchi> result = kimchiEntities.stream()
                .map(KimchiEntity::toModel)
                .map(SingleKimchi::from)
                .toList();
        return Kimchis.from(result);
    }

    @Override
    public Kimchi findById(Long kimchiId) {
        return kimchiJpaRepository.findById(kimchiId)
                .orElseThrow(() -> new BaseException(ErrorCode.KIMCHI_NOT_FOUND)).toModel();
    }

    @Override
    public Kimchi findByName(String kimchiName) {
        return kimchiJpaRepository.findByName(kimchiName).toModel();
    }

    @Override
    public void save(Kimchi kimchi) {
        kimchiJpaRepository.save(KimchiEntity.from(kimchi));
    }
}
