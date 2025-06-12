package com.supportkim.kimchimall.common.retry;

import com.supportkim.kimchimall.common.exception.PSPConfirmationException;
import org.springframework.stereotype.Component;

@Component("retryEvaluator")
public class RetryEvaluator {
    public boolean shouldRetry(Throwable e) {
        if (e instanceof PSPConfirmationException exception) {
            return exception.isRetryableError();
        }
        return false;
    }
}