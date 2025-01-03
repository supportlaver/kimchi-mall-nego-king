package com.supportkim.kimchimall.common.util;

import java.util.UUID;

public abstract class IdempotencyCreator {
    public static String create(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }
}
