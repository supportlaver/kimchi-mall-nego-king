package com.supportkim.kimchimall.kimchi.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class KimchiRequestDto {

    @Builder @Getter
    @AllArgsConstructor @NoArgsConstructor
    public static class PutCarts {
        private List<PutCart> carts;
    }

    @Builder @Getter
    @AllArgsConstructor @NoArgsConstructor
    public static class PutCart {
        private Long kimchiId;
        private int count;
    }

}
