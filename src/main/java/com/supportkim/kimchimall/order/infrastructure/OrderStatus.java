package com.supportkim.kimchimall.order.infrastructure;

public enum OrderStatus {

    COMPLETE("주문완료") , CANCEL("주문취소") , ING("주문처리중");

    OrderStatus(String name) { }
}
