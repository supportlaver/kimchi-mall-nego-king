package com.supportkim.kimchimall.payment.infrasturcture;

import com.supportkim.kimchimall.payment.service.dto.TossPaymentConfirmationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
        name = "tossPaymentFeignClient",         // Bean 이름
        url = "${PSP.toss.url}",                 // PSP.toss.url 값을 사용
        configuration = TossFeignClientConfig.class // 커스텀 설정
)
public interface TossPaymentFeignClient {

    // 예: 결제 승인 API
    @PostMapping(value = "/v1/payments/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    TossPaymentConfirmationResponse confirmPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> requestBody
    );
}
