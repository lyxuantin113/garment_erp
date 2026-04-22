package com.garment.erp.inventory.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(
        String poNumber,
        UUID supplierId,
        LocalDate orderDate,
        List<PurchaseOrderDetailRequest> details) {
}
