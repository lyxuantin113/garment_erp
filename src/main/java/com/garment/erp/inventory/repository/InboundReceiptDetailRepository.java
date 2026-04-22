package com.garment.erp.inventory.repository;

import com.garment.erp.inventory.domain.InboundReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboundReceiptDetailRepository extends JpaRepository<InboundReceiptDetail, UUID> {
}
