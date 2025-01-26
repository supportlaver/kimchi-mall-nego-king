package com.supportkim.kimchimall.payment.controller;

import com.supportkim.kimchimall.common.global.BaseResponse;
import com.supportkim.kimchimall.payment.controller.request.CheckoutRequest;
import com.supportkim.kimchimall.payment.controller.response.CheckoutResponse;
import com.supportkim.kimchimall.payment.service.CheckoutService;
import com.supportkim.kimchimall.payment.service.dto.CheckoutCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class CheckoutViewController {

    private final CheckoutService checkoutService;

    @GetMapping
    public String checkoutPage(CheckoutRequest request,Model model) {
        CheckoutCommand command = CheckoutCommand.from(request);
        CheckoutResponse it = checkoutService.checkout(command);

        model.addAttribute("orderId", it.getOrderId());
        model.addAttribute("orderName", it.getOrderName());
        model.addAttribute("amount", it.getAmount());

        return "checkout";
    }
}
