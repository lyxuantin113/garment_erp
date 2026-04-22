package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quản lý nhà cung cấp vật tư.
 */
@Entity
@Table(name = "suppliers", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String location;
}
