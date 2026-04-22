package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialIssueDetailResponse(
    UUID id,
    UUID materialId,
    String materialCode,
    String materialName,
    BigDecimal requestedQuantity,
    BigDecimal actualIssuedQuantity
) {
}
