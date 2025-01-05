package com.supportkim.kimchimall.kimchi.domain;

public enum KimchiType {
    B("배추김치") , R("깍두기") , GO("파김치");
    private final String name;
    KimchiType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
