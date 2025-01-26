package com.supportkim.kimchimall.payment.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmRequest;
import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmTest;
import com.supportkim.kimchimall.payment.controller.response.PaymentConfirmationResult;
import com.supportkim.kimchimall.payment.service.PaymentConfirmService;
import com.supportkim.kimchimall.payment.service.PaymentConfirmTestService;
import com.supportkim.kimchimall.payment.service.dto.PaymentConfirmCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 성능 테스트 전용 API
 */
@RequestMapping("/test")
@RestController @Slf4j
@RequiredArgsConstructor
public class TossPaymentTestController {

    private final PaymentConfirmTestService paymentConfirmTestService;

    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse<PaymentConfirmationResult>> testConfirm(@RequestBody TossPaymentConfirmTest request) {
        TossPaymentConfirmTest command = TossPaymentConfirmTest.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(paymentConfirmTestService.testConfirm(command)));
    }

    @PostMapping("/confirm-transactional")
    public ResponseEntity<BaseResponse<PaymentConfirmationResult>> testConfirmTransactional(@RequestBody TossPaymentConfirmTest request) {
        TossPaymentConfirmTest command = TossPaymentConfirmTest.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(paymentConfirmTestService.testConfirmTransactional(command)));
    }

    @PostMapping("/confirm-eda")
    public ResponseEntity<BaseResponse<PaymentConfirmationResult>> testConfirmEDA(@RequestBody TossPaymentConfirmTest request) {
        TossPaymentConfirmTest command = TossPaymentConfirmTest.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(paymentConfirmTestService.testConfirmEDA(command)));
    }

    @PostMapping("/confirm-kafka")
    public ResponseEntity<BaseResponse<PaymentConfirmationResult>> testConfirmKafka(@RequestBody TossPaymentConfirmTest request) {
        TossPaymentConfirmTest command = TossPaymentConfirmTest.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(paymentConfirmTestService.testConfirmKafka(command)));
    }

}
