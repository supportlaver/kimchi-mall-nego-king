package com.supportkim.kimchimall.delivery.infrastructure;

import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.delivery.domain.Delivery;
import com.supportkim.kimchimall.member.infrastructure.Address;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.*;

@Entity
@Table(name = "deliveries")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;

    @Enumerated(value = STRING)
    private DeliveryStatus deliveryStatus;

    public static DeliveryEntity from(Delivery delivery) {
        return DeliveryEntity.builder()
                .id(delivery.getId())
                .address(delivery.getAddress())
                .deliveryStatus(delivery.getDeliveryStatus())
                .build();

    }

    public Delivery toModel() {
        return Delivery.builder()
                .id(id)
                .deliveryStatus(deliveryStatus)
                .address(address)
                .build();

    }
}
