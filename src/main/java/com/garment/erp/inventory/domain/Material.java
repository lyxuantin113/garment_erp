package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quản lý vật tư (Vải, Phụ liệu...).
 */
@Entity
@Table(name = "materials", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Material extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_unit")
    private String baseUnit; // Ví dụ: MÉT, CÁI, KG

    private String note;
}
