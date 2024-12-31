package com.supportkim.kimchimall.delivery.domain;

import com.supportkim.kimchimall.delivery.infrastructure.DeliveryStatus;
import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.member.infrastructure.Address;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class Delivery {
    private Long id;
    private Address address;
    private DeliveryStatus deliveryStatus;

    public static Delivery from (Member member) {
        return Delivery
                .builder()
                .address(member.getAddress())
                .deliveryStatus(DeliveryStatus.BEFORE)
                .build();
    }
}
