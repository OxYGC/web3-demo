package com.web3.controller;

import com.web3.dto.ApiResponse;
import com.web3.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取所有账号列表
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllAccounts() {
        try {
            List<Map<String, Object>> accounts = accountService.getAllAccounts();
            return ApiResponse.success("获取账号列表成功", accounts);
        } catch (Exception e) {
            return ApiResponse.error("获取账号列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建新账号（助记词）
     */
    @PostMapping("/create-mnemonic")
    public ApiResponse<Map<String, Object>> createMnemonicAccount() {
        try {
            Map<String, Object> result = accountService.createAccount("mnemonic");
            return ApiResponse.success("账号创建成功", result);
        } catch (Exception e) {
            return ApiResponse.error("创建账号失败: " + e.getMessage());
        }
    }

    /**
     * 创建新账号（私钥）
     */
    @PostMapping("/create-privatekey")
    public ApiResponse<Map<String, Object>> createPrivateKeyAccount() {
        try {
            Map<String, Object> result = accountService.createAccount("privatekey");
            return ApiResponse.success("账号创建成功", result);
        } catch (Exception e) {
            return ApiResponse.error("创建账号失败: " + e.getMessage());
        }
    }

    /**
     * 删除账号（及其关联的钱包地址）
     */
    @DeleteMapping("/{accountId}")
    public ApiResponse<Boolean> deleteAccount(@PathVariable String accountId) {
        try {
            boolean deleted = accountService.deleteAccount(accountId);
            if (deleted) {
                return ApiResponse.success("账号删除成功", true);
            } else {
                return ApiResponse.error("账号不存在: " + accountId);
            }
        } catch (Exception e) {
            return ApiResponse.error("删除账号失败: " + e.getMessage());
        }
    }
}
