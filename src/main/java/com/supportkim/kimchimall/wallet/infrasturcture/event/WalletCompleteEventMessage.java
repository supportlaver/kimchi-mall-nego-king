package com.supportkim.kimchimall.wallet.infrasturcture.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletCompleteEventMessage {
    private String orderId;
}
