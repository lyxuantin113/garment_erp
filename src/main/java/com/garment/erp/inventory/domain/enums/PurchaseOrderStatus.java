package com.garment.erp.inventory.domain.enums;

import lombok.Getter;

@Getter
public enum PurchaseOrderStatus {
    PLANNED("PLANNED"),
    APPROVED("APPROVED"),
    PARTIAL("PARTIAL"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String value;

    PurchaseOrderStatus(String value) {
        this.value = value;
    }
}
