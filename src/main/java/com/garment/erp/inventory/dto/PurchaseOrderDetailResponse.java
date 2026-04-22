package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderDetailResponse(
    UUID id,
    UUID materialId,
    String materialCode,
    String materialName,
    BigDecimal orderedQuantity,
    BigDecimal receivedQuantity,
    BigDecimal unitPrice,
    boolean isFullyReceived
) {
}
