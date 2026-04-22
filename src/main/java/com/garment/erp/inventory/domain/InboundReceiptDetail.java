package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "inbound_receipt_details", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InboundReceiptDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_receipt_id", nullable = false)
    private InboundReceipt inboundReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "qc_status")
    private String qcStatus; // e.g., PASSED, REJECTED

    private String note;
}
