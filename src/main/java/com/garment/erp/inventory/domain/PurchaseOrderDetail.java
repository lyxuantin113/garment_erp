package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_details", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "ordered_quantity", nullable = false)
    private BigDecimal orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    public boolean isFullyReceived() {
        return receivedQuantity != null && receivedQuantity.compareTo(orderedQuantity) >= 0;
    }

    public BigDecimal getRemainingQuantity() {
        return orderedQuantity.subtract(receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO);
    }
}
