package com.supportkim.kimchimall.order.infrastructure;

import com.supportkim.kimchimall.order.domain.Order;
import com.supportkim.kimchimall.order.service.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.from(order)).toModel();
    }
}
