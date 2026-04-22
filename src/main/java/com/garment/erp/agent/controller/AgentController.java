package com.garment.erp.agent.controller;

import com.garment.erp.agent.service.ErpAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ErpAgentService erpAgentService;

    @PostMapping("/chat")
    public String chatWithAgent(@RequestBody Map<String, String> request) {
        // Lấy câu hỏi từ JSON body, ví dụ: {"message": "Trạng thái ISSUED làm thay đổi table nào?"}
        String message = request.getOrDefault("message", "Xin chào AI!");
        return erpAgentService.chat(message);
    }
}
