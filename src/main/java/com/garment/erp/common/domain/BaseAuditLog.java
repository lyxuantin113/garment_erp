package com.garment.erp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class cho các bảng Audit Log của từng Service.
 * 
 * Mỗi service sẽ có một bảng audit riêng (ví dụ: inventory.audit_logs).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditLog extends BaseEntity {

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "action", nullable = false)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "old_payload", columnDefinition = "TEXT")
    private String oldPayload;

    @Column(name = "new_payload", columnDefinition = "TEXT")
    private String newPayload;
}
