package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ActualIssueDetailRequest(
    UUID materialId,
    BigDecimal actualQuantity
) {
}
