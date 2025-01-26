package com.supportkim.kimchimall.payment.controller.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckoutRequest {
    private Long cartId = 3L;
    private List<Long> kimchiIds = List.of(1L,2L,3L);
    private Long buyerId = 4L;
    private String seed = LocalDateTime.now().toString();

}
