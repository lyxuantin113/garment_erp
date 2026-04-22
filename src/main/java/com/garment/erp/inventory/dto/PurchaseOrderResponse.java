package com.garment.erp.inventory.dto;

import com.garment.erp.inventory.domain.enums.PurchaseOrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
    UUID id,
    String poNumber,
    UUID supplierId,
    String supplierName,
    LocalDate orderDate,
    PurchaseOrderStatus status,
    List<PurchaseOrderDetailResponse> details
) {
}
