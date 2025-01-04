package com.supportkim.kimchimall.wallet.infrasturcture.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletCompleteEventMessage {
    private String orderId;
}
