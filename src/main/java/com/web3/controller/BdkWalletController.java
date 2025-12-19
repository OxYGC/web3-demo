package com.web3.controller;

import com.web3.service.BdkWalletService;
import org.bitcoindevkit.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * BDK钱包REST控制器
 * 提供比特币钱包相关的HTTP API
 */
@RestController
@RequestMapping("/api/bdk")
@CrossOrigin(origins = "*")
public class BdkWalletController {
    
    @Autowired
    private BdkWalletService bdkWalletService;
    
    /**
     * 生成助记词
     */
    @PostMapping("/generate-mnemonic")
    public ResponseEntity<Map<String, Object>> generateMnemonic(@RequestBody Map<String, Object> request) {
        try {
            int wordCount = (Integer) request.getOrDefault("wordCount", 12);
            String mnemonic = bdkWalletService.generateMnemonic(wordCount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "助记词生成成功",
                "data", Map.of(
                    "mnemonic", mnemonic,
                    "wordCount", wordCount
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "助记词生成失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证助记词
     */
    @PostMapping("/validate-mnemonic")
    public ResponseEntity<Map<String, Object>> validateMnemonic(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            boolean isValid = bdkWalletService.validateMnemonic(mnemonic);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "mnemonic", mnemonic,
                    "isValid", isValid
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "验证失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取钱包信息
     */
    @PostMapping("/wallet-info")
    public ResponseEntity<Map<String, Object>> getWalletInfo(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            Map<String, Object> walletInfo = bdkWalletService.getWalletInfo(mnemonic, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "钱包信息获取成功",
                "data", walletInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "获取钱包信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取钱包余额
     */
    @PostMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            Map<String, Object> balance = bdkWalletService.getWalletBalance(mnemonic, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "余额查询成功",
                "data", balance
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "余额查询失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取新地址
     */
    @PostMapping("/new-address")
    public ResponseEntity<Map<String, Object>> getNewAddress(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            String address = bdkWalletService.getNewAddress(mnemonic, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "地址生成成功",
                "data", Map.of(
                    "address", address,
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "地址生成失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取地址列表
     */
    @PostMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            int count = (Integer) request.getOrDefault("count", 5);
            
            List<String> addresses = bdkWalletService.getAddresses(mnemonic, network, count);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "地址列表获取成功",
                "data", Map.of(
                    "addresses", addresses,
                    "count", addresses.size(),
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "地址列表获取失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 发送比特币
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendBitcoin(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            String toAddress = (String) request.get("toAddress");
            long amount = ((Number) request.get("amount")).longValue();
            Float feeRate = request.containsKey("feeRate") ? 
                ((Number) request.get("feeRate")).floatValue() : null;
            
            String txid = bdkWalletService.sendBitcoin(mnemonic, network, toAddress, amount, feeRate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "交易发送成功",
                "data", Map.of(
                    "txid", txid,
                    "toAddress", toAddress,
                    "amount", amount,
                    "feeRate", feeRate,
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "交易发送失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取交易历史
     */
    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            List<Map<String, Object>> transactions = bdkWalletService.getTransactionHistory(mnemonic, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "交易历史获取成功",
                "data", Map.of(
                    "transactions", transactions,
                    "count", transactions.size(),
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "交易历史获取失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 估算交易手续费
     */
    @PostMapping("/estimate-fee")
    public ResponseEntity<Map<String, Object>> estimateFee(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            String toAddress = (String) request.get("toAddress");
            long amount = ((Number) request.get("amount")).longValue();
            float feeRate = ((Number) request.getOrDefault("feeRate", 1.0)).floatValue();
            
            long estimatedFee = bdkWalletService.estimateTransactionFee(mnemonic, network, toAddress, amount, feeRate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "手续费估算成功",
                "data", Map.of(
                    "estimatedFee", estimatedFee,
                    "feeRate", feeRate,
                    "amount", amount,
                    "toAddress", toAddress,
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "手续费估算失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证地址
     */
    @PostMapping("/validate-address")
    public ResponseEntity<Map<String, Object>> validateAddress(@RequestBody Map<String, Object> request) {
        try {
            String address = (String) request.get("address");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            boolean isValid = bdkWalletService.validateAddress(address, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "address", address,
                    "network", networkStr,
                    "isValid", isValid
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "地址验证失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 同步钱包
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncWallet(@RequestBody Map<String, Object> request) {
        try {
            String mnemonic = (String) request.get("mnemonic");
            String networkStr = (String) request.getOrDefault("network", "TESTNET");
            Network network = Network.valueOf(networkStr.toUpperCase());
            
            bdkWalletService.syncWallet(mnemonic, network);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "钱包同步成功",
                "data", Map.of(
                    "network", networkStr
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "钱包同步失败: " + e.getMessage()
            ));
        }
    }
}
