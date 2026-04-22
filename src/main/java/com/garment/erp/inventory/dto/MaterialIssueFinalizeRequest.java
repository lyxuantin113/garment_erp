package com.garment.erp.inventory.dto;

import java.util.List;

public record MaterialIssueFinalizeRequest(
    List<ActualIssueDetailRequest> actuals
) {
}
