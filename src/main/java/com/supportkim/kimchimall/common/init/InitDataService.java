package com.supportkim.kimchimall.common.init;

import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.domain.KimchiType;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitDataService {

    private final KimchiInit kimchiInit;
    private final KimchiJpaRepository kimchiJpaRepository;

    @PostConstruct
    public void init() {
        if (kimchiJpaRepository.findAll().size() == 0) {
            kimchiInit.saveInitData();
        }
    }

    @RequiredArgsConstructor
    @Component
    static class KimchiInit {

        private final KimchiJpaRepository kimchiJpaRepository;

        @Transactional
        public void saveInitData() {
            kimchiJpaRepository.save(KimchiEntity.builder()
                    .name("배추김치")
                    .type(KimchiType.B)
                    .price(10000)
                    .quantity(100)
                    .build());

            kimchiJpaRepository.save(KimchiEntity.builder()
                    .name("깍두기")
                    .type(KimchiType.R)
                    .price(15000)
                    .quantity(100)
                    .build());

            kimchiJpaRepository.save(KimchiEntity.builder()
                    .name("파김치")
                    .type(KimchiType.GO)
                    .price(13000)
                    .quantity(100)
                    .build());

            kimchiJpaRepository.save(KimchiEntity.builder()
                    .name("네고왕김치")
                    .type(KimchiType.B)
                    .price(12000)
                    .quantity(100)
                    .build());
        }




    }





}
