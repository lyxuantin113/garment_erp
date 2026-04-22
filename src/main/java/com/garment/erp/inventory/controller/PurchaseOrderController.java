package com.garment.erp.inventory.controller;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.inventory.dto.PurchaseOrderResponse;
import com.garment.erp.inventory.dto.PurchaseOrderRequest;
import com.garment.erp.inventory.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @PostMapping
    public ApiResponse<PurchaseOrderResponse> createPO(@RequestBody PurchaseOrderRequest request) {
        return ApiResponse.success(poService.createPurchaseOrder(request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseOrderResponse> approvePO(@PathVariable UUID id) {
        return ApiResponse.success(poService.approvePurchaseOrder(id));
    }
}
