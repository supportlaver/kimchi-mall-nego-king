package com.supportkim.kimchimall.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class SingletonObjectMapper {
    private static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
    private SingletonObjectMapper() {
    }
    public static ObjectMapper getInstance() {
        return om;
    }
}
