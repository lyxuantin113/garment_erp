package com.garment.erp.common.test;

import com.garment.erp.common.exception.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommonModuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSuccessResponse_ShouldReturnSnakeCase() throws Exception {
        mockMvc.perform(get("/api/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Kiểm tra snake_case: firstName -> first_name
                .andExpect(jsonPath("$.data.first_name").value("Xuan"))
                .andExpect(jsonPath("$.data.last_name").value("Tin"))
                .andExpect(jsonPath("$.data.joined_at").exists());
    }

    @Test
    void testBusinessError_ShouldReturnStructuredError() throws Exception {
        mockMvc.perform(get("/api/test/error/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("Đây là lỗi nghiệp vụ giả lập"));
    }

    @Test
    void testSystemError_ShouldReturnGenericError() throws Exception {
        mockMvc.perform(get("/api/test/error/system"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.SYSTEM_ERROR.getCode()));
    }
}
