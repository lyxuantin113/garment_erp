package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "material_issue_details", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class MaterialIssueDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MaterialIssueTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "requested_quantity", nullable = false)
    private BigDecimal requestedQuantity;

    @Column(name = "actual_issued_quantity")
    private BigDecimal actualIssuedQuantity = BigDecimal.ZERO;
}
