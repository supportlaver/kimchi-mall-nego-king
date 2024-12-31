package com.supportkim.kimchimall.order.service.port;

import com.supportkim.kimchimall.order.domain.Order;

public interface OrderRepository {
    Order save(Order order);
}
