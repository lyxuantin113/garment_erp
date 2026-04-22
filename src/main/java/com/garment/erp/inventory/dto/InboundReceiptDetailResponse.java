package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InboundReceiptDetailResponse(
    UUID id,
    UUID materialId,
    String materialName,
    BigDecimal quantity,
    String note
) {
}
