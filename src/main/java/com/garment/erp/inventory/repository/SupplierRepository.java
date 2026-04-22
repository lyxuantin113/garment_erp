package com.garment.erp.inventory.repository;

import com.garment.erp.inventory.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
}
