package com.garment.erp.common.test;

import com.garment.erp.common.dto.ApiResponse;
import com.garment.erp.common.exception.BaseException;
import com.garment.erp.common.exception.CommonErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * TestController dùng để demo toàn bộ sức mạnh của Module Common.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Test thành công: Trả về TestDto.
     * Bạn hãy để ý các field camelCase sẽ biến thành snake_case trong JSON.
     */
    @GetMapping("/success")
    public ApiResponse<TestDto> testSuccess() {
        TestDto data = new TestDto(
                "Xuan",
                "Tin",
                "tin@garment.com",
                LocalDateTime.now());
        return ApiResponse.success(data);
    }

    /**
     * Test lỗi nghiệp vụ: Chủ động throw BaseException.
     */
    @GetMapping("/error/business")
    public ApiResponse<Void> testBusinessError() {
        throw new BaseException(CommonErrorCode.BAD_REQUEST, "Đây là lỗi nghiệp vụ giả lập");
    }

    /**
     * Test lỗi hệ thống: Throw lỗi Runtime bất kỳ.
     */
    @GetMapping("/error/system")
    public ApiResponse<Void> testSystemError() {
        throw new RuntimeException("Oops! Một lỗi hệ thống bất ngờ xảy ra.");
    }
}
