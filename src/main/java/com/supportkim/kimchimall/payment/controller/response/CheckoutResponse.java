package com.supportkim.kimchimall.payment.controller.response;

import lombok.Data;

@Data
public class CheckoutResponse {
    private Long orderId;
    private String orderName;
    private Long amount;
}
