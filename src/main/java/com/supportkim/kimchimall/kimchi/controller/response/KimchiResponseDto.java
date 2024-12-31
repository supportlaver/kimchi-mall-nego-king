package com.supportkim.kimchimall.kimchi.controller.response;

import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class KimchiResponseDto {

    @Builder @NoArgsConstructor @AllArgsConstructor
    @Getter
    public static class Kimchis {

        private List<SingleKimchi> singleKimchiList;

        public static Kimchis from(List<SingleKimchi> singleKimchiList) {
            return Kimchis.builder()
                    .singleKimchiList(singleKimchiList)
                    .build();
        }
    }

    @Builder @NoArgsConstructor
    @Getter @AllArgsConstructor
    public static class SingleKimchi {
        private int price;
        private String name;
        private Long kimchiId;

        public static SingleKimchi from(Kimchi kimchi) {
            return SingleKimchi.builder()
                    .kimchiId(kimchi.getId())
                    .price(kimchi.getPrice())
                    .name(kimchi.getName())
                    .build();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
