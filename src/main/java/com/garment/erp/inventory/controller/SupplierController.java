package com.garment.erp.inventory.controller;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.inventory.dto.SupplierRequest;
import com.garment.erp.inventory.dto.SupplierResponse;
import com.garment.erp.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý Nhà cung cấp.
 */
@RestController
@RequestMapping("/api/inventory/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ApiResponse<SupplierResponse> createSupplier(@RequestBody SupplierRequest request) {
        return ApiResponse.success(supplierService.createSupplier(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getSupplier(@PathVariable UUID id) {
        return ApiResponse.success(supplierService.getSupplier(id));
    }

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        return ApiResponse.success(supplierService.getAllSuppliers());
    }
}
