package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import com.garment.erp.inventory.domain.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrder extends BaseEntity {

    @Column(name = "po_number", unique = true, nullable = false)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    public void addDetail(PurchaseOrderDetail detail) {
        details.add(detail);
        detail.setPurchaseOrder(this);
    }
}
