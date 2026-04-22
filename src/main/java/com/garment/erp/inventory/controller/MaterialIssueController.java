package com.garment.erp.inventory.controller;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.inventory.dto.MaterialIssueFinalizeRequest;
import com.garment.erp.inventory.dto.MaterialIssueRequest;
import com.garment.erp.inventory.dto.MaterialIssueTicketResponse;
import com.garment.erp.inventory.service.MaterialIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/material-issue")
@RequiredArgsConstructor
public class MaterialIssueController {

    private final MaterialIssueService issueService;

    @PostMapping
    public ApiResponse<MaterialIssueTicketResponse> createIssueTicket(@RequestBody MaterialIssueRequest request) {
        return ApiResponse.success(issueService.createIssueTicket(request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<MaterialIssueTicketResponse> approveTicket(@PathVariable UUID id) {
        return ApiResponse.success(issueService.approveTicket(id));
    }

    @PostMapping("/{id}/issue")
    public ApiResponse<MaterialIssueTicketResponse> finalizeIssue(
            @PathVariable UUID id,
            @RequestBody MaterialIssueFinalizeRequest request) {
        return ApiResponse.success(issueService.finalizeIssue(id, request));
    }
}
