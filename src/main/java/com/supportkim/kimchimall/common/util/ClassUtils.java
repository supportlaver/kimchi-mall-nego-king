package com.supportkim.kimchimall.common.util;

public class ClassUtils {

    // 어떤 Object 가 있을 때 Class 로 Cast 를 하는 과정을 Safe 하게 해줄 수 있는 것
    public static <T> T getSafeCastInstance(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

}
