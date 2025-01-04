package com.supportkim.kimchimall.ledger.infrasturcture.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerCompleteEventMessage {
    private String orderId;
}
