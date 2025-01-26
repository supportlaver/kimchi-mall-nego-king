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
                PaymentType.NORMAL, // type
                PaymentMethod.EASY_PAY, // method
                LocalDateTime.parse("2025-01-04T18:30:26"), // approvedAt
                "배추김치, 깍두기, 파김치", // orderName
                PSPConfirmationStatus.DONE, // pspConfirmationStatus
                38000, // totalAmount
                "TossPaymentConfirmationResponse" // pspRawData (JSON string 또는 class)
        );

        PaymentExecutionResult mockResult = new PaymentExecutionResult(
                command.getPaymentKey(), // paymentKey
                command.getOrderId(), // orderId
                extraDetails,
                true, // isSuccess
                false, // isFailure
                false, // isUnknown
                false // isRetryable
        );
        return mockResult;
    }
}
