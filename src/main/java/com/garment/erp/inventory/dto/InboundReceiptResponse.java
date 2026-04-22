package com.garment.erp.inventory.dto;

import com.garment.erp.inventory.domain.enums.InboundStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InboundReceiptResponse(
    UUID id,
    UUID purchaseOrderId,
    String poNumber,
    LocalDateTime receiptDate,
    InboundStatus status,
    Boolean overReceivedApproved,
    String note,
    List<InboundReceiptDetailResponse> details
) {
}
