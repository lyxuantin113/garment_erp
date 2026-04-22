package com.garment.erp.inventory.dto;

import java.util.List;
import java.util.UUID;

public record InboundReceiptRequest(
        UUID purchaseOrderId,
        String note,
        List<InboundReceiptDetailRequest> details) {
}
