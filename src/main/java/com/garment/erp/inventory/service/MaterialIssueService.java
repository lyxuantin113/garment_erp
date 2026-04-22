package com.garment.erp.inventory.service;

import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.inventory.domain.*;
import com.garment.erp.inventory.domain.enums.MaterialIssueStatus;
import com.garment.erp.inventory.domain.enums.TransactionType;
import com.garment.erp.inventory.dto.*;
import com.garment.erp.inventory.repository.MaterialIssueTicketRepository;
import com.garment.erp.inventory.repository.MaterialRepository;
import com.garment.erp.inventory.repository.MaterialStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialIssueService {

    private final MaterialIssueTicketRepository ticketRepository;
    private final MaterialRepository materialRepository;
    private final MaterialStockRepository stockRepository;
    private final InventoryTransactionService transactionService;

    @Transactional
    public MaterialIssueTicketResponse createIssueTicket(MaterialIssueRequest request) {
        MaterialIssueTicket ticket = new MaterialIssueTicket();
        ticket.setTicketCode("MIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setProductionOrderId(request.productionOrderId());
        ticket.setNote(request.note());
        ticket.setStatus(MaterialIssueStatus.REQUESTED);

        List<UUID> materialIds = request.details().stream()
                .map(MaterialIssueDetailRequest::materialId)
                .toList();

        Map<UUID, Material> materialMap = materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        for (var detailReq : request.details()) {
            Material material = materialMap.get(detailReq.materialId());
            if (material == null) {
                throw new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Material not found: " + detailReq.materialId());
            }

            MaterialIssueDetail detail = new MaterialIssueDetail();
            detail.setMaterial(material);
            detail.setRequestedQuantity(detailReq.requestedQuantity());
            ticket.addDetail(detail);
        }

        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public MaterialIssueTicketResponse approveTicket(UUID ticketId) {
        MaterialIssueTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() != MaterialIssueStatus.REQUESTED) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Only REQUESTED tickets can be approved");
        }

        ticket.setStatus(MaterialIssueStatus.APPROVED);
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public MaterialIssueTicketResponse finalizeIssue(UUID ticketId, MaterialIssueFinalizeRequest request) {
        MaterialIssueTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BaseException(CommonErrorCode.ENTITY_NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() != MaterialIssueStatus.APPROVED) {
            throw new BaseException(CommonErrorCode.BAD_REQUEST, "Only APPROVED tickets can be issued");
        }

        Map<UUID, BigDecimal> actualMap = request.actuals().stream()
                .collect(Collectors.toMap(ActualIssueDetailRequest::materialId, ActualIssueDetailRequest::actualQuantity));

        for (MaterialIssueDetail detail : ticket.getDetails()) {
            BigDecimal actualQty = actualMap.getOrDefault(detail.getMaterial().getId(), BigDecimal.ZERO);
            
            if (actualQty.compareTo(BigDecimal.ZERO) <= 0) continue;

            // 1. Kiểm tra tồn kho
            MaterialStock stock = stockRepository.findByMaterialId(detail.getMaterial().getId())
                    .orElseThrow(() -> new BaseException(CommonErrorCode.BAD_REQUEST, 
                            "Vật tư " + detail.getMaterial().getName() + " không có tồn kho."));

            if (stock.getBalance().compareTo(actualQty) < 0) {
                throw new BaseException(CommonErrorCode.BAD_REQUEST, 
                        "Không đủ tồn kho cho " + detail.getMaterial().getName() + ". Hiện có: " + stock.getBalance());
            }

            // 2. Cập nhật detail
            detail.setActualIssuedQuantity(actualQty);

            // 3. Ghi nhận giao dịch và trừ tồn kho (Sử dụng số lượng ÂM)
            transactionService.createTransaction(
                    detail.getMaterial(),
                    TransactionType.OUTBOUND,
                    actualQty.negate(),
                    "MATERIAL_ISSUE_TICKET",
                    ticket.getId(),
                    "Xuất kho sản xuất cho đơn " + ticket.getProductionOrderId()
            );
        }

        ticket.setStatus(MaterialIssueStatus.ISSUED);
        return mapToResponse(ticketRepository.save(ticket));
    }

    private MaterialIssueTicketResponse mapToResponse(MaterialIssueTicket ticket) {
        List<MaterialIssueDetailResponse> details = ticket.getDetails().stream()
                .map(d -> new MaterialIssueDetailResponse(
                        d.getId(),
                        d.getMaterial().getId(),
                        d.getMaterial().getCode(),
                        d.getMaterial().getName(),
                        d.getRequestedQuantity(),
                        d.getActualIssuedQuantity()))
                .toList();

        return new MaterialIssueTicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getProductionOrderId(),
                ticket.getStatus(),
                ticket.getNote(),
                details);
    }
}
