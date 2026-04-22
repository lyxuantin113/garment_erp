package com.garment.erp.inventory.repository;

import com.garment.erp.inventory.domain.MaterialStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialStockRepository extends JpaRepository<MaterialStock, UUID> {
    Optional<MaterialStock> findByMaterialId(UUID materialId);
}
