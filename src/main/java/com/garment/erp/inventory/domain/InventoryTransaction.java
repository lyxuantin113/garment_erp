package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import com.garment.erp.inventory.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal quantity; // Positive for INBOUND, negative for OUTBOUND

    @Column(name = "reference_type")
    private String referenceType; // e.g. INBOUND_RECEIPT, PRODUCTION_ISSUE

    @Column(name = "reference_id")
    private UUID referenceID;

    private String note;
}
