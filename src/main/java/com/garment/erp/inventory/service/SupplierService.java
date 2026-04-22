package com.garment.erp.inventory.service;

import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.common.util.JsonUtils;
import com.garment.erp.inventory.domain.InventoryAuditLog;
import com.garment.erp.inventory.domain.Supplier;
import com.garment.erp.inventory.dto.SupplierRequest;
import com.garment.erp.inventory.dto.SupplierResponse;
import com.garment.erp.inventory.repository.InventoryAuditLogRepository;
import com.garment.erp.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final InventoryAuditLogRepository auditLogRepository;
    private final JsonUtils jsonUtils;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        // 1. Validate phone and email uniqueness
        if (supplierRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Số điện thoại đã tồn tại: " + request.phoneNumber());
        }
        if (supplierRepository.existsByEmail(request.email())) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Email đã tồn tại: " + request.email());
        }

        // 2. Map DTO to Entity
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setPhoneNumber(request.phoneNumber());
        supplier.setEmail(request.email());
        supplier.setLocation(request.location());

        // 3. Save
        Supplier savedSupplier = supplierRepository.save(supplier);

        // 4. Audit Log
        saveAuditLog(savedSupplier.getId(), "CREATE", null, jsonUtils.toJson(request));

        return mapToResponse(savedSupplier);
    }

    public SupplierResponse getSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Không tìm thấy nhà cung cấp ID: " + id));
        return mapToResponse(supplier);
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void saveAuditLog(UUID entityId, String action, String oldPayload, String newPayload) {
        InventoryAuditLog log = new InventoryAuditLog();
        log.setEntityName("Supplier");
        log.setEntityId(entityId.toString());
        log.setAction(action);
        log.setOldPayload(oldPayload);
        log.setNewPayload(newPayload);
        auditLogRepository.save(log);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhoneNumber(),
                supplier.getEmail(),
                supplier.getLocation(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt(),
                supplier.getVersion()
        );
    }
}
