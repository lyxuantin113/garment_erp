package com.garment.erp.inventory.dto;

import java.util.List;
import java.util.UUID;

public record MaterialIssueRequest(
    UUID productionOrderId,
    String note,
    List<MaterialIssueDetailRequest> details
) {
}
