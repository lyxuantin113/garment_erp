package com.garment.erp.inventory.service;

import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.common.util.JsonUtils;
import com.garment.erp.inventory.domain.InventoryAuditLog;
import com.garment.erp.inventory.domain.Material;
import com.garment.erp.inventory.dto.MaterialRequest;
import com.garment.erp.inventory.dto.MaterialResponse;
import com.garment.erp.inventory.repository.InventoryAuditLogRepository;
import com.garment.erp.inventory.repository.MaterialRepository;
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
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final InventoryAuditLogRepository auditLogRepository;
    private final JsonUtils jsonUtils;

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        // 1. Validate unique code
        if (materialRepository.existsByCode(request.code())) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Mã vật tư đã tồn tại: " + request.code());
        }

        // 2. Map DTO to Entity
        Material material = new Material();
        material.setCode(request.code());
        material.setName(request.name());
        material.setBaseUnit(request.baseUnit());
        material.setNote(request.note());

        // 3. Save
        Material savedMaterial = materialRepository.save(material);

        // 4. Audit Log (Hành động CREATE)
        saveAuditLog(savedMaterial.getId(), "CREATE", null, jsonUtils.toJson(request));

        return mapToResponse(savedMaterial);
    }

    public MaterialResponse getMaterial(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Không tìm thấy vật tư ID: " + id));
        return mapToResponse(material);
    }

    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void saveAuditLog(UUID entityId, String action, String oldPayload, String newPayload) {
        InventoryAuditLog log = new InventoryAuditLog();
        log.setEntityName("Material");
        log.setEntityId(entityId.toString());
        log.setAction(action);
        log.setOldPayload(oldPayload);
        log.setNewPayload(newPayload);
        auditLogRepository.save(log);
    }

    private MaterialResponse mapToResponse(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getCode(),
                material.getName(),
                material.getBaseUnit(),
                material.getNote(),
                material.getCreatedAt(),
                material.getUpdatedAt(),
                material.getVersion()
        );
    }
}
