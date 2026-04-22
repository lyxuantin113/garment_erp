package com.garment.erp.inventory.dto;

import java.math.BigDecimal;

public record FabricRollRequest(
    String rollCode,
    String shadeLot,
    BigDecimal length,
    String binLocation
) {
}
