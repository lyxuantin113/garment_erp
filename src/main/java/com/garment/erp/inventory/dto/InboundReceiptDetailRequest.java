package com.garment.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InboundReceiptDetailRequest(
    UUID materialId,
    BigDecimal quantity,
    String note,
    List<FabricRollRequest> fabricRolls
) {
}
