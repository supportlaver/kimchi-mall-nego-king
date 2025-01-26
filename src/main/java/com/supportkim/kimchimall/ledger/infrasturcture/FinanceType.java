package com.supportkim.kimchimall.ledger.infrasturcture;

public enum FinanceType {
    PAYMENT_ORDER("결제 주문");

    private final String description;

    FinanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
