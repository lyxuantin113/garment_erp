package com.garment.erp.inventory.repository;

import com.garment.erp.inventory.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {
    boolean existsByCode(String code);
}
