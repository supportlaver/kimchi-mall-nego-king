package com.supportkim.kimchimall.payment.service;

import com.supportkim.kimchimall.kimchi.controller.port.KimchiService;
import com.supportkim.kimchimall.kimchi.infrastructure.KimchiEntity;
import com.supportkim.kimchimall.payment.controller.response.CheckoutResponse;
import com.supportkim.kimchimall.payment.service.dto.CheckoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final KimchiService kimchiService;


    public CheckoutResponse checkout(CheckoutCommand command) {

        List<KimchiEntity> kimchis = kimchiService.getKimchis(command.getKimchiIds());

        return new CheckoutResponse();
    }
}
