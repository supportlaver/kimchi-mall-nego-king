package com.supportkim.kimchimall.delivery.service.port;

import com.supportkim.kimchimall.delivery.domain.Delivery;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
}
