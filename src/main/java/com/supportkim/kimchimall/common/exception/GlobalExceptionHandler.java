package com.supportkim.kimchimall.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> BaseException(BaseException e) {
        e.printStackTrace();
        ErrorCode errorCode = ErrorCode.findByMessage(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.of(Objects.requireNonNull(errorCode)));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> BaseException(PessimisticLockingFailureException e) {
        e.printStackTrace();
        ErrorCode errorCode = ErrorCode.findByMessage(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(Objects.requireNonNull(errorCode)));
    }

    @ExceptionHandler(PSPConfirmationException.class)
    public ResponseEntity<ErrorResponse> handlePSPConfirmationException(PSPConfirmationException e) {
        log.error("결제 처리 중 재시도 실패: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.findByMessage(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(Objects.requireNonNull(errorCode)));
    }
}
