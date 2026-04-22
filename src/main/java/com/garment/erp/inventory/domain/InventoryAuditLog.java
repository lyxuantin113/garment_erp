package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseAuditLog;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bảng lưu trữ lịch sử thay đổi của riêng Inventory Service.
 */
@Entity
@Table(name = "audit_logs", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryAuditLog extends BaseAuditLog {
    // Kế thừa toàn bộ field từ BaseAuditLog
}
