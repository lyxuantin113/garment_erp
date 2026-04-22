package com.garment.erp.inventory.controller;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.inventory.dto.MaterialRequest;
import com.garment.erp.inventory.dto.MaterialResponse;
import com.garment.erp.inventory.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý Vật tư (Vải, phụ liệu...).
 */
@RestController
@RequestMapping("/api/inventory/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ApiResponse<MaterialResponse> createMaterial(@RequestBody MaterialRequest request) {
        return ApiResponse.success(materialService.createMaterial(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaterialResponse> getMaterial(@PathVariable UUID id) {
        return ApiResponse.success(materialService.getMaterial(id));
    }

    @GetMapping
    public ApiResponse<List<MaterialResponse>> getAllMaterials() {
        return ApiResponse.success(materialService.getAllMaterials());
    }
}
