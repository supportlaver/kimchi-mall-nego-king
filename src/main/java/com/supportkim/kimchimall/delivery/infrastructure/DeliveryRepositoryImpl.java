package com.supportkim.kimchimall.delivery.infrastructure;

import com.supportkim.kimchimall.delivery.domain.Delivery;
import com.supportkim.kimchimall.delivery.service.port.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {
    private final DeliveryJpaRepository deliveryJpaRepository;

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryJpaRepository.save(DeliveryEntity.from(delivery)).toModel();
    }
}
