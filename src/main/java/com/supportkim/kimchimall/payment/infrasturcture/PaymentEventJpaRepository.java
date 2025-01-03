package com.supportkim.kimchimall.payment.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentEventJpaRepository extends JpaRepository<PaymentEvent , Long> {
    Optional<PaymentEvent> findByOrderId(String orderId);
}
