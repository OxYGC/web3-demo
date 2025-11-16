package com.web3.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.web3.dto.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        log.info("收到ping请求");
        return ApiResponse.success("API服务正常运行", "pong");
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        log.info("收到info请求");

        Map<String, Object> info = new HashMap<>();
        info.put("service", "Web3 Wallet System");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());
        info.put("status", "running");

        return ApiResponse.success("获取系统信息成功", info);
    }
}
