package com.supportkim.kimchimall.payment.service.dto;


import com.supportkim.kimchimall.kimchi.infrastructure.PaymentFailure;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class PaymentStatusUpdateCommand {

    private final String paymentKey;
    private final String orderId;
    private final PaymentStatus status;
    private final PaymentExtraDetails extraDetails;
    private final PaymentFailure failure;

    public PaymentStatusUpdateCommand(
            String paymentKey,
            String orderId,
            PaymentStatus status,
            PaymentExtraDetails extraDetails,
            PaymentFailure failure
    ) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.extraDetails = extraDetails;
        this.failure = failure;

        // Validation
        if (status != PaymentStatus.SUCCESS && status != PaymentStatus.FAILURE && status != PaymentStatus.UNKNOWN) {
            throw new IllegalArgumentException(
                    "결제 상태 (status: " + status + ") 는 올바르지 않은 결제 상태입니다."
            );
        }

        if (status == PaymentStatus.SUCCESS && extraDetails == null) {
            throw new IllegalArgumentException(
                    "PaymentStatus 값이 SUCCESS 라면 PaymentExtraDetails 는 Null 이 되면 안 됩니다."
            );
        }

        if (status == PaymentStatus.FAILURE && failure == null) {
            throw new IllegalArgumentException(
                    "PaymentStatus 값이 FAILURE 라면 PaymentExecutionFailure 는 Null 이 되면 안 됩니다."
            );
        }
    }

    public PaymentStatusUpdateCommand(PaymentExecutionResult paymentExecutionResult) {
        this(
                paymentExecutionResult.getPaymentKey(),
                paymentExecutionResult.getOrderId(),
                paymentExecutionResult.paymentStatus(),
                paymentExecutionResult.getExtraDetails(),
                paymentExecutionResult.getFailure()
        );
    }

    public static PaymentStatusUpdateCommand from(PaymentExecutionResult req) {
        return PaymentStatusUpdateCommand.builder()
                .paymentKey(req.getPaymentKey())
                .orderId(req.getOrderId())
                .status(req.paymentStatus())
                .extraDetails(req.getExtraDetails())
                .failure(req.getFailure())
                .build();
    }

}
