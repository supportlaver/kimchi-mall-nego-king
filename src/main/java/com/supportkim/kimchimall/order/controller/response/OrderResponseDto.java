package com.supportkim.kimchimall.order.controller.response;

import com.supportkim.kimchimall.member.domain.Member;
import com.supportkim.kimchimall.order.controller.request.OrderRequestDto;
import com.supportkim.kimchimall.order.domain.Order;
import com.supportkim.kimchimall.order.infrastructure.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private int totalPrice;
        private OrderStatus orderStatus;
        private String name;

        public static OrderResponse of(Order order , Member member) {
            return OrderResponse.builder()
                    .orderStatus(order.getOrderStatus())
                    .totalPrice(order.getTotalPrice())
                    .name(member.getName())
                    .build();
        }

        public static OrderResponse ordering(int totalPrice ,Member member) {
            return OrderResponse.builder()
                    .orderStatus(OrderStatus.ING)
                    .totalPrice(totalPrice)
                    .name(member.getName())
                    .build();
        }
    }
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class CheckoutResponse {
        private Long buyerMemberId;
        private String seed;
    }
}
