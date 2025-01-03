package com.supportkim.kimchimall.payment.controller.response;

import com.supportkim.kimchimall.kimchi.infrastructure.PaymentFailure;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentConfirmationResult {

    private final PaymentStatus status;
    private final PaymentFailure failure;
    private final String message;

    public PaymentConfirmationResult(PaymentStatus status, PaymentFailure failure) {
        if (status == PaymentStatus.FAILURE && failure == null) {
            throw new IllegalArgumentException(
                    "결제 상태 FAILURE 일 때 PaymentFailure 는 null 값이 될 수 없습니다."
            );
        }

        this.status = status;
        this.failure = failure;

        // 메시지 초기화
        switch (status) {
            case SUCCESS:
                this.message = "결제 처리에 성공하였습니다.";
                break;
            case FAILURE:
                this.message = "결제 처리에 실패하였습니다.";
                break;
            case UNKNOWN:
                this.message = "결제 처리 중에 알 수 없는 에러가 발생했습니다.";
                break;
            default:
                throw new IllegalStateException(
                        "현재 결제 상태 (status: " + status + ") 는 올바르지 않은 상태 입니다."
                );
        }
    }
}