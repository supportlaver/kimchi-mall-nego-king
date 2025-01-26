package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.payment.controller.request.TossPaymentConfirmTest;
import com.supportkim.kimchimall.payment.service.dto.PSPConfirmationStatus;
import com.supportkim.kimchimall.payment.service.dto.PaymentConfirmCommand;
import com.supportkim.kimchimall.payment.service.dto.PaymentExecutionResult;
import com.supportkim.kimchimall.payment.service.dto.PaymentExtraDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MockTossPaymentExecutor {
    public PaymentExecutionResult execute(TossPaymentConfirmTest command) {
        PaymentExtraDetails extraDetails = new PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.parse("2025-01-04T18:30:26"),
                "배추김치, 깍두기, 파김치",
                PSPConfirmationStatus.DONE,
                38000,
                "TossPaymentConfirmationResponse"
        );

        PaymentExecutionResult mockResult = new PaymentExecutionResult(
                command.getPaymentKey(),
                command.getOrderId(),
                extraDetails,
                true,
                false,
                false,
                false
        );
        return mockResult;
    }
}
