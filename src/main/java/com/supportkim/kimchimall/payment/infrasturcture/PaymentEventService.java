package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.common.exception.BaseException;
import com.supportkim.kimchimall.common.exception.ErrorCode;
import com.supportkim.kimchimall.ledger.infrasturcture.event.LedgerCompleteEventMessage;
import com.supportkim.kimchimall.wallet.infrasturcture.event.WalletCompleteEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {
    private final PaymentEventJpaRepository paymentEventRepository;
    @Transactional
    public void handleWalletCompleteEvent(WalletCompleteEventMessage event) {
        PaymentEvent paymentEvent = paymentEventRepository.findWithOrdersByOrderId(event.getOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_PAYMENT_EVENT));
    }

    @Transactional
    public void handleLedgerCompleteEvent(LedgerCompleteEventMessage event) {
        PaymentEvent paymentEvent = paymentEventRepository.findWithOrdersByOrderId(event.getOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_PAYMENT_EVENT));
    }
}
