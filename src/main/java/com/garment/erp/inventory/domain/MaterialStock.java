package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "material_stocks", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class MaterialStock extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", unique = true, nullable = false)
    private Material material;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public void addBalance(BigDecimal amount) {
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        this.balance = this.balance.add(amount);
    }
}
