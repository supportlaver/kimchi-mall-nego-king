package com.supportkim.kimchimall.member.infrastructure;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable @Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Address {
    // 도로명 코드
    private String rnMgtSn;
    // 읍면동 일련번호
    private int emdNo;
}
