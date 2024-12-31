package com.supportkim.kimchimall.order.service;

import com.supportkim.kimchimall.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제 서버 Mock
 */
@Service
@Slf4j
public class MockService {

    // 결제 성공 : True
    // 결제 실패 : False
    public boolean payment(Order order) {
        return true;
    }
    public void sendEmail(Order order) {
        log.info("Success Send Email");
    }

}