package com.supportkim.kimchimall.outbox.infrastructure;

import com.supportkim.kimchimall.outbox.domain.Outbox;
import com.supportkim.kimchimall.outbox.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends JpaRepository<Outbox , Long> {
    Optional<Outbox> findByIdempotencyKey(String idempotencyKey);
    Optional<Outbox> findByIdempotencyKeyAndType(String idempotencyKey, String type);
    @Query("""
           SELECT o FROM Outbox o
           WHERE (o.status = 'INIT' OR o.status = 'FAILURE')
           AND o.createdAt <= :threshold
           AND o.type = :type
           """)
    List<Outbox> findPendingMessages(@Param("threshold") LocalDateTime threshold,
                                     @Param("type") String type);


    List<Outbox> findByStatusInAndTypeAndCreatedAtBefore(
            List<OutboxStatus> statuses,
            String type,
            LocalDateTime createdAtBefore
    );
}
