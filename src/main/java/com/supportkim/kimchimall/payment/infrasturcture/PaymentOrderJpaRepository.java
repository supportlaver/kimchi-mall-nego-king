package com.supportkim.kimchimall.payment.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderJpaRepository extends JpaRepository<PaymentOrder , Long> {
}
