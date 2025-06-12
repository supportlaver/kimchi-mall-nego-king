package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.payment.service.dto.PendingPayment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderJpaRepository extends JpaRepository<PaymentOrder , Long> {
    @Query("SELECT p.id, p.paymentStatus FROM PaymentOrder p WHERE p.orderId = :orderId")
    List<Object[]> findPaymentOrderStatusByOrderId(@Param("orderId") String orderId);

    List<PaymentOrder> findByOrderId(String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentOrder p WHERE p.orderId = :orderId")
    List<PaymentOrder> findByOrderIdWithLock(@Param("orderId") String orderId);

    @Query("SELECT SUM(p.amount) FROM PaymentOrder p WHERE p.orderId = :orderId")
    Integer findTotalAmountByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Query("UPDATE PaymentOrder p SET p.paymentStatus = :status WHERE p.orderId = :orderId")
    int updateStatusByOrderId(@Param("orderId") String orderId, @Param("status") PaymentStatus status);


    @Query(value = """
        SELECT pe.id AS paymentEventId,
               pe.payment_key AS paymentKey,
               pe.order_id AS orderId,
               po.id AS paymentOrderId,
               po.payment_order_status AS paymentOrderStatus,
               po.amount AS amount,
               po.failed_count AS failedCount,
               po.threshold AS threshold
        FROM payment_orders po
        INNER JOIN payment_events pe ON po.payment_event_id = pe.id
        WHERE (po.payment_order_status = 'UNKNOWN' 
               OR (po.payment_order_status = 'EXECUTING' AND po.updated_at <= :updatedAt))
          AND po.failed_count < po.threshold
        LIMIT 10
        """, nativeQuery = true)
    List<PendingPayment> findPendingPayments(@Param("updatedAt") LocalDateTime updatedAt);
}
