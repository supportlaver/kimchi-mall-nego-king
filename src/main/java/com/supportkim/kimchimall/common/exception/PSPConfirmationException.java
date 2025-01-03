package com.supportkim.kimchimall.common.exception;

import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;

public class PSPConfirmationException extends RuntimeException {
    private String errorCode;
    private String errorMessage;
    private boolean isSuccess;
    private boolean isFailure;
    private boolean isUnknown;
    private boolean isRetryableError;

    public PSPConfirmationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PSPConfirmationException(
            String errorCode,
            String errorMessage,
            boolean isSuccess,
            boolean isFailure,
            boolean isUnknown,
            boolean isRetryableError,
            Throwable cause
    ) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
        this.isRetryableError = isRetryableError;

        // Validation
        if ((isSuccess ? 1 : 0) + (isFailure ? 1 : 0) + (isUnknown ? 1 : 0) != 1) {
            throw new IllegalArgumentException(
                    PSPConfirmationException.class.getSimpleName() + " 는 올바르지 않은 결제 상태를 가지고 있습니다."
            );
        }
    }

    public PSPConfirmationException(
            String errorCode,
            String errorMessage,
            boolean isSuccess,
            boolean isFailure,
            boolean isUnknown,
            boolean isRetryableError
    ) {
        this(errorCode, errorMessage, isSuccess, isFailure, isUnknown, isRetryableError, null);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
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

    public boolean isRetryableError() {
        return isRetryableError;
    }

    public PaymentStatus paymentStatus() {
        if (isSuccess) {
            return PaymentStatus.SUCCESS;
        } else if (isFailure) {
            return PaymentStatus.FAILURE;
        } else if (isUnknown) {
            return PaymentStatus.UNKNOWN;
        } else {
            throw new IllegalStateException(
                    PSPConfirmationException.class.getSimpleName() + " 는 올바르지 않은 결제 상태를 가지고 있습니다."
            );
        }
    }
}
