package com.supportkim.kimchimall.ledger.infrasturcture.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LedgerCompleteEventMessage {
    private String orderId;
}
