package com.supportkim.kimchimall.payment.infrasturcture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderJpaRepository extends JpaRepository<PaymentOrder , Long> {
    @Query("SELECT p.id, p.paymentStatus FROM PaymentOrder p WHERE p.orderId = :orderId")
    List<Object[]> findPaymentOrderStatusByOrderId(String orderId);

    List<PaymentOrder> findByOrderId(String orderId);

    @Query("SELECT SUM(p.amount) FROM PaymentOrder p WHERE p.orderId = :orderId")
    Integer findTotalAmountByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Query("UPDATE PaymentOrder p SET p.paymentStatus = :status WHERE p.orderId = :orderId")
    int updateStatusByOrderId(@Param("orderId") String orderId, @Param("status") PaymentStatus status);
}
