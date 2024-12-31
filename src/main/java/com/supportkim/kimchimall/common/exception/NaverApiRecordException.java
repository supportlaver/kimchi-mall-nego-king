package com.supportkim.kimchimall.common.exception;

import feign.FeignException;
import feign.Request;

import java.util.Collection;
import java.util.Map;

public class NaverApiRecordException extends FeignException.TooManyRequests {
    public NaverApiRecordException(String message, Request request, byte[] body, Map<String, Collection<String>> headers) {
        super(message, request, body, headers);
    }
}
