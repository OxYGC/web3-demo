package com.web3.controller;

import com.web3.dto.ApiResponse;
import com.web3.dto.BalanceRequest;
import com.web3.dto.BalanceResponse;
import com.web3.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * 查询单个地址余额
     */
    @PostMapping("/query")
    public ApiResponse<BalanceResponse> queryBalance(@Valid @RequestBody BalanceRequest request) {
        try {
            log.info("收到余额查询请求: address={}, coinType={}", request.getAddress(), request.getCoinType());
            
            BalanceResponse balance = balanceService.getBalance(request);
            
            if (balance.isSuccess()) {
                return ApiResponse.success("余额查询成功", balance);
            } else {
                return ApiResponse.error("余额查询失败: " + balance.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("余额查询异常", e);
            return ApiResponse.error("余额查询异常: " + e.getMessage());
        }
    }

    /**
     * 批量查询多个地址余额
     */
    @PostMapping("/batch-query")
    public ApiResponse<List<BalanceResponse>> batchQueryBalance(@RequestBody List<@Valid BalanceRequest> requests) {
        try {
            log.info("收到批量余额查询请求，数量: {}", requests.size());
            
            List<BalanceResponse> responses = new ArrayList<>();
            for (BalanceRequest request : requests) {
                BalanceResponse balance = balanceService.getBalance(request);
                responses.add(balance);
            }
            
            return ApiResponse.success("批量余额查询完成", responses);
        } catch (Exception e) {
            log.error("批量余额查询异常", e);
            return ApiResponse.error("批量余额查询异常: " + e.getMessage());
        }
    }

    /**
     * 快速查询 - 通过GET请求查询余额
     */
    @GetMapping("/quick-query")
    public ApiResponse<BalanceResponse> quickQueryBalance(
            @RequestParam String address,
            @RequestParam String coinType,
            @RequestParam(defaultValue = "mainnet") String network) {
        try {
            log.info("收到快速余额查询请求: address={}, coinType={}", address, coinType);
            
            BalanceRequest request = new BalanceRequest();
            request.setAddress(address);
            request.setCoinType(coinType);
            request.setNetwork(network);
            
            BalanceResponse balance = balanceService.getBalance(request);
            
            if (balance.isSuccess()) {
                return ApiResponse.success("余额查询成功", balance);
            } else {
                return ApiResponse.error("余额查询失败: " + balance.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("快速余额查询异常", e);
            return ApiResponse.error("余额查询异常: " + e.getMessage());
        }
    }
}
