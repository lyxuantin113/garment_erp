package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderDetailRequest(
    UUID materialId,
    BigDecimal orderedQuantity,
    BigDecimal unitPrice
) {
}
