package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialIssueDetailRequest(
    UUID materialId,
    BigDecimal requestedQuantity
) {
}
