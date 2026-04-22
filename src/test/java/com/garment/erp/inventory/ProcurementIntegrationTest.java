package com.garment.erp.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garment.erp.common.exception.CommonErrorCode;
import com.garment.erp.inventory.domain.*;
import com.garment.erp.inventory.domain.enums.InboundStatus;
import com.garment.erp.inventory.domain.enums.PurchaseOrderStatus;
import com.garment.erp.inventory.dto.*;
import com.garment.erp.inventory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProcurementIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MaterialRepository materialRepository;

        @Autowired
        private SupplierRepository supplierRepository;

        @Autowired
        private PurchaseOrderRepository poRepository;

        @Autowired
        private InboundReceiptRepository inboundRepository;

        @Autowired
        private MaterialStockRepository stockRepository;

        @Autowired
        private InventoryTransactionRepository transactionRepository;

        @Autowired
        private FabricRollRepository fabricRollRepository;

        private Material testMaterial;
        private Supplier testSupplier;

        @BeforeEach
        void setUp() {
                // 1. Tạo vật tư mẫu
                testMaterial = new Material();
                testMaterial.setCode("FAB-001");
                testMaterial.setName("Thun Cotton Co Giãn");
                testMaterial.setBaseUnit("Meters");
                materialRepository.save(testMaterial);

                // 2. Tạo nhà cung cấp mẫu
                testSupplier = new Supplier();
                testSupplier.setName("NCC Vải Thành Công");
                testSupplier.setEmail("contact@thanhcong.vn");
                testSupplier.setPhoneNumber("0987654321");
                supplierRepository.save(testSupplier);
        }

        @Test
        @DisplayName("Case 1 & 2: Luồng PO chuẩn - Tạo, Duyệt và Nhập kho thành công")
        void testStandardProcurementFlow() throws Exception {
                // --- BƯỚC 1: TẠO PO ---
                PurchaseOrderDetailRequest detailReq = new PurchaseOrderDetailRequest(
                                testMaterial.getId(), new BigDecimal("1000"), new BigDecimal("5.5"));

                PurchaseOrderRequest poReq = new PurchaseOrderRequest(
                                "PO-2024-001", testSupplier.getId(), LocalDate.now(), List.of(detailReq));

                MvcResult poRes = mockMvc.perform(post("/api/v1/inventory/purchase-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(poReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("PLANNED"))
                                .andReturn();

                UUID poId = UUID.fromString(objectMapper.readTree(poRes.getResponse().getContentAsString()).get("data")
                                .get("id").asText());

                // --- BƯỚC 2: DUYỆT PO ---
                mockMvc.perform(post("/api/v1/inventory/purchase-orders/" + poId + "/approve"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("APPROVED"));

                // --- BƯỚC 3: TẠO PHIẾU NHẬP (INSPECTING) ---
                InboundReceiptDetailRequest receiptDetailReq = new InboundReceiptDetailRequest(
                                testMaterial.getId(), new BigDecimal("1000"), "Nhập đủ hàng", null);

                InboundReceiptRequest receiptReq = new InboundReceiptRequest(poId, "Giao đợt 1",
                                List.of(receiptDetailReq));

                MvcResult receiptRes = mockMvc.perform(post("/api/v1/inventory/inbound/receive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(receiptReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("INSPECTING"))
                                .andReturn();

                UUID receiptId = UUID.fromString(objectMapper.readTree(receiptRes.getResponse().getContentAsString())
                                .get("data").get("id").asText());

                // --- BƯỚC 4: CHỐT PHIẾU (COMPLETED) ---
                mockMvc.perform(post("/api/v1/inventory/inbound/" + receiptId + "/complete"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

                // --- BƯỚC 5: VERIFY DỮ LIỆU ---
                // Verify PO Status
                PurchaseOrder updatedPO = poRepository.findById(poId).orElseThrow();
                assertThat(updatedPO.getStatus()).isEqualTo(PurchaseOrderStatus.COMPLETED);

                // Verify Stock Balance
                MaterialStock stock = stockRepository.findByMaterialId(testMaterial.getId()).orElseThrow();
                assertThat(stock.getBalance()).isEqualByComparingTo("1000");

                // Verify Transaction Log
                List<InventoryTransaction> txs = transactionRepository.findAll();
                assertThat(txs).anyMatch(t -> t.getQuantity().toPlainString().equals("1000"));
        }

        @Test
        @DisplayName("Case 3 & 4: Luồng Nhập thừa - Chặn và Duyệt bởi quản lý")
        void testOverReceivingFlow() throws Exception {
                // Setup PO Approved 100 units
                PurchaseOrder po = createApprovedPO("PO-OVER", new BigDecimal("100"));

                // Nhập 120 units -> Mong đợi bị chặn
                InboundReceiptDetailRequest detail = new InboundReceiptDetailRequest(
                                testMaterial.getId(), new BigDecimal("120"), "Giao thừa", null);
                InboundReceiptRequest request = new InboundReceiptRequest(po.getId(), "Test Over", List.of(detail));

                MvcResult res = mockMvc.perform(post("/api/v1/inventory/inbound/receive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();
                UUID receiptId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString())
                                .get("data").get("id").asText());

                // Thử chốt phiếu -> Lỗi 400
                mockMvc.perform(post("/api/v1/inventory/inbound/" + receiptId + "/complete"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.BAD_REQUEST.getCode()));

                // Quản lý Duyệt thừa
                mockMvc.perform(post("/api/v1/inventory/inbound/" + receiptId + "/approve-excess"))
                                .andExpect(status().isOk());

                // Thử chốt lại -> Thành công
                mockMvc.perform(post("/api/v1/inventory/inbound/" + receiptId + "/complete"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

                // Verify PO là COMPLETED (vì đã vượt record)
                assertThat(poRepository.findById(po.getId()).get().getStatus())
                                .isEqualTo(PurchaseOrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("Case 5: Giao hàng nhiều đợt (Partial Shipment)")
        void testPartialShipmentFlow() throws Exception {
                // PO đặt 1000 units
                PurchaseOrder po = createApprovedPO("PO-PARTIAL", new BigDecimal("1000"));

                // Đợt 1: Giao 400 units
                UUID receipt1 = createAndCompleteInbound(po.getId(), new BigDecimal("400"));
                assertThat(poRepository.findById(po.getId()).get().getStatus()).isEqualTo(PurchaseOrderStatus.PARTIAL);

                // Đợt 2: Giao nốt 600 units
                createAndCompleteInbound(po.getId(), new BigDecimal("600"));
                assertThat(poRepository.findById(po.getId()).get().getStatus())
                                .isEqualTo(PurchaseOrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("Case 6: Nhập Vải kèm chi tiết các Cuộn vải (Fabric Rolls)")
        void testFabricRollInbound() throws Exception {
                PurchaseOrder po = createApprovedPO("PO-FABRIC", new BigDecimal("500"));

                // Tạo 2 cuộn: Cuộn 1 (200m), Cuộn 2 (300m) -> Tổng 500m
                FabricRollRequest roll1 = new FabricRollRequest("ROLL-001", "LOT-A", new BigDecimal("200"), "BIN-01");
                FabricRollRequest roll2 = new FabricRollRequest("ROLL-002", "LOT-A", new BigDecimal("300"), "BIN-01");

                InboundReceiptDetailRequest detail = new InboundReceiptDetailRequest(
                                testMaterial.getId(), new BigDecimal("500"), "Nhập vải cuộn", List.of(roll1, roll2));
                InboundReceiptRequest request = new InboundReceiptRequest(po.getId(), "Vải về", List.of(detail));

                mockMvc.perform(post("/api/v1/inventory/inbound/receive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // Kiểm chứng các bản ghi FabricRoll đã được tạo
                List<FabricRoll> rolls = fabricRollRepository.findAll();
                assertThat(rolls).hasSize(2);
                assertThat(rolls).extracting("rollCode").containsExactlyInAnyOrder("ROLL-001", "ROLL-002");
                assertThat(rolls.get(0).getStatus()).isEqualTo("PENDING_QC");
        }

        // --- HELPER METHODS ---

        private PurchaseOrder createApprovedPO(String poNumber, BigDecimal qty) {
                PurchaseOrder po = new PurchaseOrder();
                po.setPoNumber(poNumber);
                po.setSupplier(testSupplier);
                po.setOrderDate(LocalDate.now());
                po.setStatus(PurchaseOrderStatus.APPROVED);

                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setMaterial(testMaterial);
                detail.setOrderedQuantity(qty);
                detail.setUnitPrice(new BigDecimal("10"));
                po.addDetail(detail);

                return poRepository.save(po);
        }

        private UUID createAndCompleteInbound(UUID poId, BigDecimal qty) throws Exception {
                InboundReceiptDetailRequest detail = new InboundReceiptDetailRequest(testMaterial.getId(), qty,
                                "Partial", null);
                InboundReceiptRequest request = new InboundReceiptRequest(poId, "Partial", List.of(detail));

                MvcResult res = mockMvc.perform(post("/api/v1/inventory/inbound/receive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                UUID receiptId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString())
                                .get("data").get("id").asText());

                mockMvc.perform(post("/api/v1/inventory/inbound/" + receiptId + "/complete"))
                                .andExpect(status().isOk());

                return receiptId;
        }
}
