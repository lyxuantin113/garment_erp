package com.garment.erp.inventory.domain;

import com.garment.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "fabric_rolls", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class FabricRoll extends BaseEntity {

    @Column(name = "roll_code", unique = true, nullable = false)
    private String rollCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_roll_id")
    private FabricRoll parentRoll;

    @Column(name = "shade_lot")
    private String shadeLot;

    @Column(name = "original_length", precision = 19, scale = 4)
    private BigDecimal originalLength;

    @Column(name = "current_length", precision = 19, scale = 4)
    private BigDecimal currentLength;

    @Column(name = "bin_location")
    private String binLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_receipt_id")
    private InboundReceipt inboundReceipt;

    @Column(nullable = false)
    private String status;
}
