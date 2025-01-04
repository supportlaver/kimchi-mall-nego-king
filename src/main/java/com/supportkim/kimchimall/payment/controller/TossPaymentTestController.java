package com.supportkim.kimchimall.payment.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmRequest;
import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmTest;
import com.supportkim.kimchimall.payment.controller.response.PaymentConfirmationResult;
import com.supportkim.kimchimall.payment.service.PaymentConfirmService;
import com.supportkim.kimchimall.payment.service.dto.PaymentConfirmCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController @Slf4j
@RequiredArgsConstructor
public class TossPaymentTestController {

    private final PaymentConfirmService paymentConfirmService;

    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse<PaymentConfirmationResult>> testConfirm(@RequestBody TossPaymentConfirmTest request) {
        log.info("confirm 호출");
        log.info("confirmTest = {} " , request);
        TossPaymentConfirmTest command = TossPaymentConfirmTest.from(request);
        return ResponseEntity.ok().body(new BaseResponse<>(paymentConfirmService.testConfirm(command)));
    }

}
