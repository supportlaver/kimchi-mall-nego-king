package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.outbox.domain.PaymentOutboxEventMessage;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

@Component
@RequiredArgsConstructor
public class PaymentEventOutboxMessagePublisher {

    private final ApplicationEventPublisher publisher;

    public void publishEvent(PaymentOutboxEventMessage event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    publisher.publishEvent(event);
                }
            });
        } else {
            publisher.publishEvent(event);
        }
    }
}
