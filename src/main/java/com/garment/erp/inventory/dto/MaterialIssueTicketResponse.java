package com.garment.erp.inventory.dto;

import com.garment.erp.inventory.domain.enums.MaterialIssueStatus;

import java.util.List;
import java.util.UUID;

public record MaterialIssueTicketResponse(
    UUID id,
    String ticketCode,
    UUID productionOrderId,
    MaterialIssueStatus status,
    String note,
    List<MaterialIssueDetailResponse> details
) {
}
