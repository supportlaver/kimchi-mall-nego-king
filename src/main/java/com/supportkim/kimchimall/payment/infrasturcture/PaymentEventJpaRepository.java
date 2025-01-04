package com.supportkim.kimchimall.payment.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentEventJpaRepository extends JpaRepository<PaymentEvent , Long> {
    Optional<PaymentEvent> findByOrderId(String orderId);

    @Query("SELECT pe FROM PaymentEvent pe JOIN FETCH pe.paymentOrders po WHERE pe.orderId = :orderId")
            Optional<PaymentEvent> findWithOrdersByOrderId(@Param("orderId") String orderId);
}
