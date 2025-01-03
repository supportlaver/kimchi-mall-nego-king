package com.supportkim.kimchimall.payment.service.dto;

import com.supportkim.kimchimall.kimchi.infrastructure.PaymentFailure;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;
import lombok.Data;

import java.util.Objects;

@Data
public class PaymentExecutionResult {
    private String paymentKey;
    private String orderId;
    private PaymentExtraDetails extraDetails;
    private PaymentFailure failure;
    private boolean isSuccess;
    private boolean isFailure;
    private boolean isUnknown;
    private boolean isRetryable;

    public PaymentExecutionResult(
            String paymentKey,
            String orderId,
            PaymentExtraDetails extraDetails,
            boolean isSuccess,
            boolean isFailure,
            boolean isUnknown,
            boolean isRetryable
    ) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.extraDetails = extraDetails;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
        this.isRetryable = isRetryable;

        // Validation
        if ((isSuccess ? 1 : 0) + (isFailure ? 1 : 0) + (isUnknown ? 1 : 0) != 1) {
            throw new IllegalArgumentException("결제 (orderId: " + orderId + ") 는 올바르지 않은 결제 상태입니다.");
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    public boolean isRetryable() {
        return isRetryable;
    }

    public PaymentStatus paymentStatus() {
        if (isSuccess) {
            return PaymentStatus.SUCCESS;
        } else if (isFailure) {
            return PaymentStatus.FAILURE;
        } else if (isUnknown) {
            return PaymentStatus.UNKNOWN;
        } else {
            throw new IllegalStateException("결제 (orderId : " + orderId + ") 는 올바르지 않은 결제 상태입니다.");
        }
    }
}