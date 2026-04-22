package com.garment.erp.inventory.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trạng thái của Phiếu nhập hàng (Inbound Receipt)
 * Đồng nhất theo TABLES_STATES.md
 */
@Getter
@RequiredArgsConstructor
public enum InboundStatus {
    PENDING("Hàng tới bãi/kho"),
    INSPECTING("Đang kiểm KCS đầu vào"),
    COMPLETED("Chốt phiếu - Đã cập nhật kho"),
    CANCELLED("Đã hủy");

    private final String description;
}
