package com.garment.erp.inventory.service;

import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.inventory.domain.*;
import com.garment.erp.inventory.domain.enums.InboundStatus;
import com.garment.erp.inventory.domain.enums.PurchaseOrderStatus;
import com.garment.erp.inventory.domain.enums.TransactionType;
import com.garment.erp.inventory.dto.*;
import com.garment.erp.inventory.repository.FabricRollRepository;
import com.garment.erp.inventory.repository.InboundReceiptRepository;
import com.garment.erp.inventory.repository.MaterialRepository;
import com.garment.erp.inventory.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundService {

    private final InboundReceiptRepository inboundRepository;
    private final PurchaseOrderRepository poRepository;
    private final MaterialRepository materialRepository;
    private final InventoryTransactionService transactionService;
    private final FabricRollRepository fabricRollRepository;

    @Transactional
    public InboundReceiptResponse processInbound(InboundReceiptRequest request) {
        PurchaseOrder po = poRepository.findById(request.purchaseOrderId())
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND,
                        "PO not found: " + request.purchaseOrderId()));

        if (po.getStatus() == PurchaseOrderStatus.COMPLETED || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST,
                    "Cannot receive materials for a COMPLETED or CANCELLED PO");
        }

        List<UUID> materialIds = request.details().stream()
                .map(InboundReceiptDetailRequest::materialId)
                .toList();

        Map<UUID, Material> materialMap = materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        Map<UUID, PurchaseOrderDetail> poDetailMap = po.getDetails().stream()
                .collect(Collectors.toMap(d -> d.getMaterial().getId(), d -> d));

        InboundReceipt receipt = new InboundReceipt();
        receipt.setPurchaseOrder(po);
        receipt.setReceiptDate(LocalDateTime.now());
        receipt.setNote(request.note());
        receipt.setStatus(InboundStatus.INSPECTING);

        for (InboundReceiptDetailRequest detailReq : request.details()) {
            Material material = materialMap.get(detailReq.materialId());
            if (material == null) {
                throw new BaseException(CommonErrorCode.ENTITY_NOT_FOUND,
                        "Material not found: " + detailReq.materialId());
            }

            if (!poDetailMap.containsKey(material.getId())) {
                throw new BaseException(CommonErrorCode.BAD_REQUEST,
                        "Material " + material.getName() + " is not in the PO");
            }

            InboundReceiptDetail receiptDetail = new InboundReceiptDetail();
            receiptDetail.setMaterial(material);
            receiptDetail.setQuantity(detailReq.quantity());
            receiptDetail.setNote(detailReq.note());
            receipt.addDetail(receiptDetail);

            if (detailReq.fabricRolls() != null && !detailReq.fabricRolls().isEmpty()) {
                for (var rollReq : detailReq.fabricRolls()) {
                    FabricRoll roll = new FabricRoll();
                    roll.setRollCode(rollReq.rollCode());
                    roll.setMaterial(material);
                    roll.setShadeLot(rollReq.shadeLot());
                    roll.setOriginalLength(rollReq.length());
                    roll.setCurrentLength(rollReq.length());
                    roll.setBinLocation(rollReq.binLocation());
                    roll.setStatus("PENDING_QC");
                    roll.setInboundReceipt(receipt);
                    fabricRollRepository.save(roll);
                }
            }
        }

        return mapToResponse(inboundRepository.save(receipt));
    }

    /**
     * Xác nhận hoàn thành phiếu nhập và cập nhật tồn kho.
     * Chặn nếu nhập thừa mà chưa được duyệt.
     */
    @Transactional
    public InboundReceiptResponse completeInbound(UUID receiptId) {
        InboundReceipt receipt = inboundRepository.findById(receiptId)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Inbound Receipt not found"));

        if (receipt.getStatus() != InboundStatus.INSPECTING) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Only INSPECTING receipts can be completed");
        }

        PurchaseOrder po = receipt.getPurchaseOrder();
        Map<UUID, PurchaseOrderDetail> poDetailMap = po.getDetails().stream()
                .collect(Collectors.toMap(d -> d.getMaterial().getId(), d -> d));

        boolean hasExcess = false;
        for (InboundReceiptDetail detail : receipt.getDetails()) {
            PurchaseOrderDetail poDetail = poDetailMap.get(detail.getMaterial().getId());
            BigDecimal newReceivedTotal = poDetail.getReceivedQuantity().add(detail.getQuantity());

            if (newReceivedTotal.compareTo(poDetail.getOrderedQuantity()) > 0) {
                hasExcess = true;
                break;
            }
        }

        if (hasExcess && !Boolean.TRUE.equals(receipt.getOverReceivedApproved())) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST,
                    "Phát hiện hàng nhập thừa so với PO. Cần Quản lý phê duyệt trước khi chốt phiếu.");
        }

        receipt.setStatus(InboundStatus.COMPLETED);
        InboundReceipt savedReceipt = inboundRepository.save(receipt);

        finalizeInbound(savedReceipt);
        return mapToResponse(savedReceipt);
    }

    private InboundReceiptResponse mapToResponse(InboundReceipt receipt) {
        List<InboundReceiptDetailResponse> details = receipt.getDetails().stream()
                .map(d -> new InboundReceiptDetailResponse(
                        d.getId(),
                        d.getMaterial().getId(),
                        d.getMaterial().getName(),
                        d.getQuantity(),
                        d.getNote()))
                .toList();

        return new InboundReceiptResponse(
                receipt.getId(),
                receipt.getPurchaseOrder().getId(),
                receipt.getPurchaseOrder().getPoNumber(),
                receipt.getReceiptDate(),
                receipt.getStatus(),
                receipt.getOverReceivedApproved(),
                receipt.getNote(),
                details);
    }

    /**
     * Phê duyệt việc nhập thừa cho một phiếu nhập.
     */
    @Transactional
    public void approveExcess(UUID receiptId) {
        InboundReceipt receipt = inboundRepository.findById(receiptId)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Inbound Receipt not found"));

        receipt.setOverReceivedApproved(true);
        inboundRepository.save(receipt);
    }

    private void finalizeInbound(InboundReceipt receipt) {
        PurchaseOrder po = receipt.getPurchaseOrder();

        // Tối ưu tìm kiếm PO Detail
        java.util.Map<UUID, PurchaseOrderDetail> poDetailMap = po.getDetails().stream()
                .collect(java.util.stream.Collectors.toMap(d -> d.getMaterial().getId(), d -> d));

        for (InboundReceiptDetail detail : receipt.getDetails()) {
            // 1. Cập nhật số lượng đã nhận trong PO Detail
            PurchaseOrderDetail poDetail = poDetailMap.get(detail.getMaterial().getId());
            if (poDetail == null)
                continue; // Nên log cảnh báo nếu null

            poDetail.setReceivedQuantity(poDetail.getReceivedQuantity().add(detail.getQuantity()));

            // 2. Tạo giao dịch kho (Tự động cập nhật Stock Balance bên trong service)
            transactionService.createTransaction(
                    detail.getMaterial(),
                    TransactionType.INBOUND,
                    detail.getQuantity(),
                    "INBOUND_RECEIPT",
                    receipt.getId(),
                    "Inbound from PO " + po.getPoNumber());
        }

        // 3. Cập nhật trạng thái PO
        updatePOStatus(po);
        poRepository.save(po);
    }

    private void updatePOStatus(PurchaseOrder po) {
        boolean allReceived = po.getDetails().stream()
                .allMatch(d -> d.getReceivedQuantity().compareTo(d.getOrderedQuantity()) >= 0);

        if (allReceived) {
            po.setStatus(PurchaseOrderStatus.COMPLETED);
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIAL);
        }
    }
}
