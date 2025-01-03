package com.supportkim.kimchimall.common.exception;

import com.supportkim.kimchimall.payment.infrasturcture.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class PaymentAlreadyProcessedException extends RuntimeException{
    private String message;
    private PaymentStatus status;
}
