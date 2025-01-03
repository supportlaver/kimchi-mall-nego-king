package com.supportkim.kimchimall.payment.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventJpaRepository extends JpaRepository<PaymentEvent , Long> {
}
