package com.supportkim.kimchimall.payment.infrasturcture;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentErrorHandler {

    private final StreamBridge streamBridge;

    @ServiceActivator(inputChannel = "payment-out-0.errors")
    public void handleError(ErrorMessage errorMessage) {
        // 오류 정보 추출
        MessagingException exception = (MessagingException) errorMessage.getPayload();
        Message<?> failedMessage = exception.getFailedMessage();

        // DLQ 토픽으로 메시지 재전송
        streamBridge.send("dlq-topic-out-0", failedMessage.getPayload());
    }
}
