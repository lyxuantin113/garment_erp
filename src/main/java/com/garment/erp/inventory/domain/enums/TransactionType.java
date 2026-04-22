package com.garment.erp.inventory.domain.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    INBOUND("INBOUND"),
    OUTBOUND("OUTBOUND"),
    ADJUSTMENT("ADJUSTMENT"),
    TRANSFER("TRANSFER"),
    RETURN("RETURN"),
    ISSUE_TO_PRODUCTION("ISSUE_TO_PRODUCTION");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }
}
