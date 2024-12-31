package com.supportkim.kimchimall.cart.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supportkim.kimchimall.cartkimchi.domain.CartKimchi;
import com.supportkim.kimchimall.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cart {
    private Long id;
    private int quantity;
    private List<CartKimchi> cartKimchis = new ArrayList<>();
    /*@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;*/

    public void clear() {
        this.cartKimchis.clear();
        this.quantity = 0;
    }
}
