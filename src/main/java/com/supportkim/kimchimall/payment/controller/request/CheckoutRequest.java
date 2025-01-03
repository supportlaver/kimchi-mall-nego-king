package com.supportkim.kimchimall.payment.controller.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클라이언트에서 이런 요청이 넘어와야한다.
 */
@Data
public class CheckoutRequest {
    private Long cartId = 3L;
    private List<Long> kimchiIds = List.of(1L,2L,3L);
    private Long buyerId = 4L;
    // LocalDateTime.now().toString()
    private String seed = LocalDateTime.now().toString();

}
