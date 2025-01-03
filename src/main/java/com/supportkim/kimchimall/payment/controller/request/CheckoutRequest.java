package com.supportkim.kimchimall.payment.controller.request;

import lombok.Data;

import java.util.List;

/**
 * 클라이언트에서 이런 요청이 넘어와야한다.
 */
@Data
public class CheckoutRequest {
    private Long cartId;
    private List<Long> kimchiIds;
    private Long buyerId;
    // LocalDateTime.now().toString()
    private String seed;
}
