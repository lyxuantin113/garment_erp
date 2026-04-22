package com.garment.erp.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BaseEntity là class nền tảng cho mọi thực thể trong hệ thống ERP.
 * 
 * Tại sao dùng abstract? Vì chúng ta không bao giờ khởi tạo trực tiếp BaseEntity.
 * Tại sao dùng @MappedSuperclass? Để các field này được "nhúng" vào table của các class con (như Material, Order).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Sử dụng UUID làm Primary Key thay vì Long/BigInt.
     * Lý do: Trong Microservices, UUID giúp tránh xung đột ID khi tổng hợp dữ liệu từ nhiều nguồn
     * và bảo mật hơn (không thể đoán được ID tiếp theo là gì).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * @CreatedDate: Tự động điền thời gian khi bản ghi được tạo.
     * columnDefinition: "TIMESTAMP" để chỉ định chính xác kiểu dữ liệu trong DB.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @LastModifiedDate: Tự động cập nhật mỗi khi bản ghi được update.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * @CreatedBy: Lưu ID của người thực hiện hành động tạo.
     * Hiện tại chúng ta dùng UUID, sau này sẽ lấy từ Security Context (JWT).
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    /**
     * @Version: Cơ chế Optimistic Locking.
     * Giúp ngăn chặn tình trạng "Lost Update": khi 2 người cùng sửa 1 bản ghi, 
     * người submit sau sẽ bị lỗi nếu dữ liệu đã bị thay đổi trước đó.
     */
    @Version
    private Long version;

    /**
     * Soft Delete: Thay vì xóa cứng, chúng ta đánh dấu là đã xóa.
     * Giúp phục hồi dữ liệu khi cần và giữ tính toàn vẹn của Audit Log.
     */
    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
