package com.garment.erp.inventory.service;

import com.garment.erp.inventory.domain.InventoryTransaction;
import com.garment.erp.inventory.domain.Material;
import com.garment.erp.inventory.domain.MaterialStock;
import com.garment.erp.inventory.domain.enums.TransactionType;
import com.garment.erp.inventory.repository.InventoryTransactionRepository;
import com.garment.erp.inventory.repository.MaterialStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final MaterialStockRepository stockRepository;

    /**
     * Thực hiện giao dịch kho và cập nhật số dư tồn kho.
     * Tuân thủ nguyên tắc IMMUTABLE_INVENTORY.
     */
    @Transactional
    public InventoryTransaction createTransaction(
            Material material,
            TransactionType type,
            BigDecimal quantity,
            String referenceType,
            UUID referenceId,
            String note) {
        // 1. Tạo bản ghi Transaction (Audit Trail)
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setMaterial(material);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceID(referenceId);
        transaction.setNote(note);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        // 2. Cập nhật số dư MaterialStock
        MaterialStock stock = stockRepository.findByMaterialId(material.getId())
                .orElseGet(() -> {
                    MaterialStock newStock = new MaterialStock();
                    newStock.setMaterial(material);
                    newStock.setBalance(BigDecimal.ZERO);
                    return newStock;
                });

        stock.addBalance(quantity);
        stockRepository.save(stock);

        return savedTransaction;
    }
}
