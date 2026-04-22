package com.garment.erp.inventory.controller;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.inventory.dto.InboundReceiptResponse;
import com.garment.erp.inventory.dto.InboundReceiptRequest;
import com.garment.erp.inventory.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @PostMapping("/receive")
    public ApiResponse<InboundReceiptResponse> receiveMaterials(@RequestBody InboundReceiptRequest request) {
        return ApiResponse.success(inboundService.processInbound(request));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<InboundReceiptResponse> completeInbound(@PathVariable UUID id) {
        return ApiResponse.success(inboundService.completeInbound(id));
    }

    @PostMapping("/{id}/approve-excess")
    public ApiResponse<Void> approveExcess(@PathVariable UUID id) {
        inboundService.approveExcess(id);
        return ApiResponse.success(null);
    }
}
