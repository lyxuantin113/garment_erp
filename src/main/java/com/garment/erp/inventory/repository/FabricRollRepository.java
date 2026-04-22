package com.garment.erp.inventory.repository;

import com.garment.erp.inventory.domain.FabricRoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FabricRollRepository extends JpaRepository<FabricRoll, UUID> {
}
