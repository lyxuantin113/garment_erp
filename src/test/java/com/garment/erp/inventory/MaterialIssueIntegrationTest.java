package com.garment.erp.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garment.erp.inventory.domain.*;
import com.garment.erp.inventory.domain.enums.MaterialIssueStatus;
import com.garment.erp.inventory.dto.*;
import com.garment.erp.inventory.repository.*;
import com.garment.erp.inventory.service.InventoryTransactionService;
import com.garment.erp.inventory.domain.enums.TransactionType;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialIssueIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MaterialRepository materialRepository;

        @Autowired
        private MaterialStockRepository stockRepository;

        @Autowired
        private InventoryTransactionService transactionService;

        @Autowired
        private MaterialIssueTicketRepository ticketRepository;

        @Autowired
        private InventoryTransactionRepository transactionRepository;

        private Material testMaterial;

        @BeforeEach
        void setUp() {
                testMaterial = new Material();
                testMaterial.setCode("MAT-ISSUE-01");
                testMaterial.setName("Nút áo 4 lỗ");
                testMaterial.setBaseUnit("PCS");
                materialRepository.save(testMaterial);

                // 1. Giả lập nhập kho 1000 PCS để có hàng xuất
                transactionService.createTransaction(
                                testMaterial,
                                TransactionType.INBOUND,
                                new BigDecimal("1000"),
                                "TEST_SETUP",
                                UUID.randomUUID(),
                                "Initial Stock");
        }

        @Test
        @DisplayName("Luồng Xuất kho chuẩn: Tạo yêu cầu -> Duyệt -> Xuất kho thành công")
        void testStandardMaterialIssueFlow() throws Exception {
                UUID prodOrderId = UUID.randomUUID();

                // 1. Tạo phiếu yêu cầu xuất 400 PCS
                MaterialIssueDetailRequest detailReq = new MaterialIssueDetailRequest(testMaterial.getId(),
                                new BigDecimal("400"));
                MaterialIssueRequest request = new MaterialIssueRequest(prodOrderId, "Cần gấp cho chuyền A",
                                List.of(detailReq));

                MvcResult res = mockMvc.perform(post("/api/v1/inventory/material-issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                                .andReturn();

                UUID ticketId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString())
                                .get("data").get("id").asText());

                // 2. Duyệt phiếu
                mockMvc.perform(post("/api/v1/inventory/material-issue/" + ticketId + "/approve"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("APPROVED"));

                // 3. Thực hiện xuất thực tế 400 PCS
                ActualIssueDetailRequest actualDetail = new ActualIssueDetailRequest(testMaterial.getId(),
                                new BigDecimal("400"));
                MaterialIssueFinalizeRequest finalizeRequest = new MaterialIssueFinalizeRequest(List.of(actualDetail));

                mockMvc.perform(post("/api/v1/inventory/material-issue/" + ticketId + "/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(finalizeRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("ISSUED"));

                // 4. Kiểm chứng tồn kho
                MaterialStock stock = stockRepository.findByMaterialId(testMaterial.getId()).orElseThrow();
                assertThat(stock.getBalance()).isEqualByComparingTo("600"); // 1000 - 400 = 600

                // 5. Kiểm chứng Ledger
                List<InventoryTransaction> txs = transactionRepository.findAll();
                assertThat(txs).anyMatch(t -> t.getType() == TransactionType.OUTBOUND
                                && t.getQuantity().compareTo(new BigDecimal("-400")) == 0);
        }

        @Test
        @DisplayName("Chặn xuất kho khi không đủ tồn kho")
        void testInsufficientStockIssue() throws Exception {
                UUID prodOrderId = UUID.randomUUID();

                // 1. Tạo phiếu yêu cầu xuất 400 (Có sẵn 1000 -> OK)
                MaterialIssueDetailRequest detailReq = new MaterialIssueDetailRequest(testMaterial.getId(),
                                new BigDecimal("400"));
                MaterialIssueRequest request = new MaterialIssueRequest(prodOrderId, "Test Block", List.of(detailReq));

                MvcResult res = mockMvc.perform(post("/api/v1/inventory/material-issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                UUID ticketId = UUID.fromString(objectMapper.readTree(res.getResponse().getContentAsString())
                                .get("data").get("id").asText());

                // 2. Duyệt phiếu
                mockMvc.perform(post("/api/v1/inventory/material-issue/" + ticketId + "/approve"));

                // 3. Thử xuất 1200 (Vượt quá 1000 hiện có)
                ActualIssueDetailRequest actualDetail = new ActualIssueDetailRequest(testMaterial.getId(),
                                new BigDecimal("1200"));
                MaterialIssueFinalizeRequest finalizeRequest = new MaterialIssueFinalizeRequest(List.of(actualDetail));

                mockMvc.perform(post("/api/v1/inventory/material-issue/" + ticketId + "/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(finalizeRequest)))
                                .andExpect(status().isBadRequest()); // Kỳ vọng lỗi 400

                // Kiểm tra tồn kho vẫn là 1000
                MaterialStock stock = stockRepository.findByMaterialId(testMaterial.getId()).orElseThrow();
                assertThat(stock.getBalance()).isEqualByComparingTo("1000");
        }
}
