package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.ledger.infrasturcture.event.LedgerCompleteEventMessage;
import com.supportkim.kimchimall.payment.service.event.PaymentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerCompleteEventListener {

    private final PaymentEventJpaRepository paymentEventRepository;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ledgerUpdate(LedgerCompleteEventMessage event) {
        PaymentEvent paymentEvent = paymentEventRepository.findWithOrdersByOrderId(event.getOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_PAYMENT_EVENT));
    }
}
