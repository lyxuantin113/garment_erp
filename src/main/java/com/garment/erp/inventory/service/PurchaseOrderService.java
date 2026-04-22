package com.garment.erp.inventory.service;

import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.inventory.domain.*;
import com.garment.erp.inventory.dto.*;
import com.garment.erp.inventory.domain.enums.PurchaseOrderStatus;
import com.garment.erp.inventory.repository.MaterialRepository;
import com.garment.erp.inventory.repository.PurchaseOrderRepository;
import com.garment.erp.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND,
                        "Supplier not found: " + request.supplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(request.poNumber());
        po.setSupplier(supplier);
        po.setOrderDate(request.orderDate());
        po.setStatus(PurchaseOrderStatus.PLANNED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<UUID> materialIds = request.details().stream()
                .map(PurchaseOrderDetailRequest::materialId)
                .toList();

        Map<UUID, Material> materialMap = materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        for (var detailRequest : request.details()) {
            Material material = materialMap.get(detailRequest.materialId());
            if (material == null) {
                throw new BaseException(CommonErrorCode.ENTITY_NOT_FOUND,
                        "Material not found: " + detailRequest.materialId());
            }

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setMaterial(material);
            detail.setOrderedQuantity(detailRequest.orderedQuantity());
            detail.setUnitPrice(detailRequest.unitPrice());

            po.addDetail(detail);

            totalAmount = totalAmount.add(detailRequest.orderedQuantity().multiply(detailRequest.unitPrice()));
        }

        po.setTotalAmount(totalAmount);
        return mapToResponse(poRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(UUID poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(
                        () -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Purchase Order not found: " + poId));

        if (po.getStatus() != PurchaseOrderStatus.PLANNED) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Only PLANNED orders can be approved");
        }

        po.setStatus(PurchaseOrderStatus.APPROVED);
        return mapToResponse(poRepository.save(po));
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        List<PurchaseOrderDetailResponse> details = po.getDetails().stream()
                .map(d -> new PurchaseOrderDetailResponse(
                        d.getId(),
                        d.getMaterial().getId(),
                        d.getMaterial().getCode(),
                        d.getMaterial().getName(),
                        d.getOrderedQuantity(),
                        d.getReceivedQuantity(),
                        d.getUnitPrice(),
                        d.isFullyReceived()))
                .toList();

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getStatus(),
                details);
    }
}
