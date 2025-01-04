package com.supportkim.kimchimall.ledger.infrasturcture;

public enum FinanceType {
    PAYMENT_ORDER("결제 주문");

    private final String description;

    // Constructor
    FinanceType(String description) {
        this.description = description;
    }

    // Getter
    public String getDescription() {
        return description;
    }

}
