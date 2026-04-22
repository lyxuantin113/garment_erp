package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import com.garment.erp.inventory.domain.enums.MaterialIssueStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "material_issue_tickets", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class MaterialIssueTicket extends BaseEntity {

    @Column(name = "ticket_code", unique = true, nullable = false)
    private String ticketCode;

    @Column(name = "production_order_id", nullable = false)
    private UUID productionOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialIssueStatus status;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    private String note;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialIssueDetail> details = new ArrayList<>();

    public void addDetail(MaterialIssueDetail detail) {
        details.add(detail);
        detail.setTicket(this);
    }
}
