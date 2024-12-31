package com.supportkim.kimchimall.orderkimchi.infrastructure;

import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.order.infrastructure.OrderEntity;
import com.supportkim.kimchimall.orderkimchi.domain.OrderKimchi;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "order_kimchis")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderKimchiEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_kimchi_id")
    private Long id;

    private int orderPrice;
    private int quantity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "orders_id")
    private OrderEntity order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "kimchi_id")
    private KimchiEntity kimchi;

    public static List<OrderKimchiEntity> fromList(List<OrderKimchi> orderKimchis) {
        return orderKimchis.stream().map(OrderKimchiEntity::from)
                .collect(Collectors.toList());
    }

    public static OrderKimchiEntity from(OrderKimchi orderKimchi) {
        return OrderKimchiEntity.builder()
                // .id(orderKimchi.getId())
                .kimchi(KimchiEntity.from(orderKimchi.getKimchi()))
                .orderPrice(orderKimchi.getPrice())
                .quantity(orderKimchi.getQuantity())
                .build();
    }

    public OrderKimchi toModel() {
        return OrderKimchi.builder()
                //.id(id)
                .kimchi(kimchi.toModel())
                //.order(order.toModel())
                .price(orderPrice)
                .quantity(quantity)
                .build();
    }

}
