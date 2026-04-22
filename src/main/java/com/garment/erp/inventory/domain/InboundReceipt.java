package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import com.garment.erp.inventory.domain.enums.InboundStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inbound_receipts", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InboundReceipt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboundStatus status;

    @Column(name = "over_received_approved")
    private Boolean overReceivedApproved = false;

    private String note;

    @OneToMany(mappedBy = "inboundReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundReceiptDetail> details = new ArrayList<>();

    public void addDetail(InboundReceiptDetail detail) {
        details.add(detail);
        detail.setInboundReceipt(this);
    }
}
