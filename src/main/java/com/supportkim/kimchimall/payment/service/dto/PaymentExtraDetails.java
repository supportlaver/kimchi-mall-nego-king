package com.supportkim.kimchimall.payment.service.dto;

import com.supportkim.kimchimall.payment.infrasturcture.PaymentMethod;
import com.supportkim.kimchimall.payment.infrasturcture.PaymentType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentExtraDetails {
    private final PaymentType type;
    private final PaymentMethod method;
    private final LocalDateTime approvedAt;
    private final String orderName;
    private final PSPConfirmationStatus pspConfirmationStatus;
    private final long totalAmount;
    private final String pspRawData;
}
