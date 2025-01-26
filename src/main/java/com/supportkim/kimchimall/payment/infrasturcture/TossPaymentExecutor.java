package com.supportkim.kimchimall.payment.infrasturcture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportkim.kimchimall.common.exception.PSPConfirmationException;
import com.supportkim.kimchimall.common.exception.TossPaymentError;
import com.supportkim.kimchimall.kimchi.infrastructure.PaymentFailure;
import com.supportkim.kimchimall.payment.service.dto.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component @Slf4j
@RequiredArgsConstructor
public class TossPaymentExecutor {

    @Value("${PSP.toss.secretKey}")
    private String secretKey;

    private final TossPaymentFeignClient tossPaymentFeignClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentExecutionResult execute(PaymentConfirmCommand command) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", command.getPaymentKey());
        requestBody.put("orderId", command.getOrderId());
        requestBody.put("amount", command.getAmount());


        try {
            TossPaymentConfirmationResponse response = tossPaymentFeignClient.confirmPayment(
                    command.getOrderId(), requestBody
            );

            return new PaymentExecutionResult(
                    command.getPaymentKey(),
                    command.getOrderId(),
                    new PaymentExtraDetails(
                            PaymentType.get(response.getType()),
                            PaymentMethod.get(response.getMethod()),
                            LocalDateTime.parse(response.getApprovedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                            response.getOrderName(),
                            PSPConfirmationStatus.get(response.getStatus()),
                            response.getTotalAmount(),
                            response.toString()
                    ),
                    true, false, false, false
            );

        } catch (Exception e) {
            if (e instanceof FeignException) {
                FeignException feignException = (FeignException) e;

                String body = feignException.contentUTF8();

                if (body == null || body.trim().isEmpty()) {
                    log.error("Feign call returned status: {}, headers: {}, body length: {}",
                            feignException.status(),
                            feignException.responseHeaders(),
                            body == null ? 0 : body.length());
                    throw new PSPConfirmationException(
                            "EMPTY_RESPONSE",
                            "No content from server.",
                            false, true, false, false
                    );
                }

                TossPaymentConfirmationResponse.TossFailureResponse failureResponse
                        = parseFailureResponse(body);

                TossPaymentError error = TossPaymentError.get(failureResponse.getCode());
                throw new PSPConfirmationException(
                        error.name(),
                        error.getDescription(),
                        error.isSuccess(),
                        error.isFailure(),
                        error.isUnknown(),
                        error.isRetryableError()
                );
            }
            throw new PSPConfirmationException("Unexpected error occurred during payment confirmation", e);
        }
    }

    public static TossPaymentConfirmationResponse.TossFailureResponse parseFailureResponse(String json) {
        try {
            return objectMapper.readValue(json, TossPaymentConfirmationResponse.TossFailureResponse.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse failure response: " + json, e);
        }
    }
}
