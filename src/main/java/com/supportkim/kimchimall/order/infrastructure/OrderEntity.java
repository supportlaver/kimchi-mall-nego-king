package com.supportkim.kimchimall.order.infrastructure;

import com.supportkim.kimchimall.common.global.BaseEntity;
import com.supportkim.kimchimall.delivery.domain.Delivery;
import com.supportkim.kimchimall.delivery.infrastructure.DeliveryEntity;
import com.supportkim.kimchimall.member.infrastructure.MemberEntity;
import com.supportkim.kimchimall.order.domain.Order;
import com.supportkim.kimchimall.orderkimchi.domain.OrderKimchi;
import com.supportkim.kimchimall.orderkimchi.infrastructure.OrderKimchiEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static java.util.stream.Collectors.*;

@Entity
@Table(name = "orders")
@Getter @Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    @Enumerated(STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order" , cascade = ALL)
    private List<OrderKimchiEntity> orderKimchis = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;
    private int totalPrice;

    public static OrderEntity from(Order order) {
        System.out.println("order123 : " + order);
        return OrderEntity.builder()
                .member(MemberEntity.fromForJoin(order.getMember()))
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .orderKimchis(OrderKimchiEntity.fromList(order.getOrderKimchis()))
                .build();
    }

    public Order toModel() {
        return Order.builder()
                .orderStatus(orderStatus)
                .member(member.toModel())
                .totalPrice(totalPrice)
                .orderKimchis(orderKimchis.stream()
                        .map(OrderKimchiEntity::toModel)
                        .collect(toList()))
                .build();
    }
}
