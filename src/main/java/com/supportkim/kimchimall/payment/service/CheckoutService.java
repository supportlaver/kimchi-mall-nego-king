package com.supportkim.kimchimall.payment.service;

import com.supportkim.kimchimall.kimchi.controller.port.KimchiService;
import com.supportkim.kimchimall.kimchi.domain.Kimchi;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.payment.controller.response.CheckoutResponse;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentEvent;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentEventJpaRepository;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentOrder;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;
import com.supportkim.kimchimall.payment.service.dto.CheckoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutService {

    private final KimchiService kimchiService;
    private final PaymentEventJpaRepository paymentEventJpaRepository;

    @Transactional
    public CheckoutResponse checkout(CheckoutCommand command) {
        List<KimchiEntity> kimchis = kimchiService.getKimchis(command.getKimchiIds());

        PaymentEvent paymentEvent = createPaymentEvent(command, kimchis);

        paymentEventJpaRepository.save(paymentEvent);

        return CheckoutResponse.from(paymentEvent);
    }


    private PaymentEvent createPaymentEvent(CheckoutCommand command, List<KimchiEntity> kimchis) {
        // PaymentOrder 리스트 생성
        List<PaymentOrder> paymentOrders = kimchis.stream()
                .map(kimchi -> PaymentOrder.of(
                        kimchi.getSellerId(),
                        command.getIdempotencyKey(),
                        kimchi.getId(),
                        kimchi.getPrice(),
                        PaymentStatus.NOT_STARTED
                ))
                .collect(Collectors.toList());

        // PaymentEvent 생성
        PaymentEvent paymentEvent = PaymentEvent.of(
                command.getBuyerId(),
                generateOrderName(kimchis),
                command.getIdempotencyKey(),
                paymentOrders
        );

        paymentOrders.forEach(order -> order.settingPaymentEvent(paymentEvent));

        return paymentEvent;
    }

    private String generateOrderName(List<KimchiEntity> kimchis) {
        return kimchis.stream()
                .map(KimchiEntity::getName)
                .collect(Collectors.joining(", "));
    }
}
