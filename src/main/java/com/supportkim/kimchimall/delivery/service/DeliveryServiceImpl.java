package com.supportkim.kimchimall.delivery.service;

import com.supportkim.kimchimall.delivery.controller.port.DeliveryService;
import com.supportkim.kimchimall.delivery.domain.Delivery;
import com.supportkim.kimchimall.delivery.service.port.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;

    @Transactional
    @Override
    public Delivery save(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }
}
