package com.web3.controller;

import com.web3.dto.*;
import com.web3.service.WalletService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@Validated
public class WalletController {

    @Resource
    private WalletService walletService;

    /**
     * 生成助记词
     */
    @PostMapping("/generate-mnemonic")
    public ApiResponse<String> generateMnemonic(@RequestBody MnemonicGenerateRequest request) {
        try {
            // 验证词数是否符合BIP39标准
            int wordCount = request.getWordCount();
            if (wordCount != 12 && wordCount != 15 && wordCount != 18 && wordCount != 21 && wordCount != 24) {
                return ApiResponse.error("助记词长度必须是12、15、18、21或24个单词");
            }

            String mnemonic = walletService.generateMnemonic(wordCount);
            return ApiResponse.success("助记词生成成功", mnemonic);

        } catch (Exception e) {
            return ApiResponse.error("生成助记词失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机私钥
     */
    @PostMapping("/generate-private-key")
    public ApiResponse<String> generatePrivateKey() {
        try {
            String privateKey = walletService.generatePrivateKey();
            return ApiResponse.success("私钥生成成功", privateKey);

        } catch (Exception e) {
            return ApiResponse.error("生成私钥失败: " + e.getMessage());
        }
    }

    /**
     * 验证助记词
     */
    @PostMapping("/validate-mnemonic")
    public ApiResponse<Boolean> validateMnemonic(@RequestBody String mnemonic) {
        try {
            boolean isValid = walletService.validateMnemonic(mnemonic.trim());
            return ApiResponse.success("助记词验证完成", isValid);

        } catch (Exception e) {
            return ApiResponse.error("验证助记词失败: " + e.getMessage());
        }
    }

    /**
     * 根据助记词批量生成钱包地址
     */
    @PostMapping("/generate-from-mnemonic")
    public ApiResponse<List<WalletInfo>> generateWalletsFromMnemonic(@Valid @RequestBody WalletGenerateRequest request) {
        try {
            // 验证助记词
            if (request.getMnemonic() == null || request.getMnemonic().trim().isEmpty()) {
                return ApiResponse.error("助记词不能为空");
            }

            if (!walletService.validateMnemonic(request.getMnemonic().trim())) {
                return ApiResponse.error("助记词格式不正确");
            }

            // 设置默认支持的区块链
            List<String> blockchains = request.getBlockchains();
            if (blockchains == null || blockchains.isEmpty()) {
                blockchains = Arrays.asList("BTC", "ETH", "SOL");
            }

            // 验证区块链类型
            List<String> supportedBlockchains = Arrays.asList("BTC", "ETH", "SOL");
            for (String blockchain : blockchains) {
                if (!supportedBlockchains.contains(blockchain.toUpperCase())) {
                    return ApiResponse.error("不支持的区块链类型: " + blockchain);
                }
            }

            List<WalletInfo> wallets = walletService.generateWalletsFromMnemonic(
                    request.getMnemonic().trim(),
                    blockchains,
                    request.getCount(),
                    request.getStartIndex()
            );

            return ApiResponse.success("钱包生成成功", wallets);

        } catch (Exception e) {
            return ApiResponse.error("生成钱包失败: " + e.getMessage());
        }
    }

    /**
     * 根据私钥批量生成钱包地址
     */
    @PostMapping("/generate-from-private-key")
    public ApiResponse<List<WalletInfo>> generateWalletsFromPrivateKey(@Valid @RequestBody WalletGenerateRequest request) {
        try {
            // 验证私钥
            if (request.getPrivateKey() == null || request.getPrivateKey().trim().isEmpty()) {
                return ApiResponse.error("私钥不能为空");
            }

            String privateKey = request.getPrivateKey().trim();

            // 简单验证私钥格式（十六进制，64字符）
            String cleanPrivateKey = privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
            if (!cleanPrivateKey.matches("^[a-fA-F0-9]{64}$")) {
                return ApiResponse.error("私钥格式不正确，应为64位十六进制字符串");
            }

            // 设置默认支持的区块链
            List<String> blockchains = request.getBlockchains();
            if (blockchains == null || blockchains.isEmpty()) {
                blockchains = Arrays.asList("BTC", "ETH", "SOL");
            }

            // 验证区块链类型
            List<String> supportedBlockchains = Arrays.asList("BTC", "ETH", "SOL");
            for (String blockchain : blockchains) {
                if (!supportedBlockchains.contains(blockchain.toUpperCase())) {
                    return ApiResponse.error("不支持的区块链类型: " + blockchain);
                }
            }

            List<WalletInfo> wallets = walletService.generateWalletsFromPrivateKey(privateKey, blockchains);

            return ApiResponse.success("钱包生成成功", wallets);

        } catch (Exception e) {
            return ApiResponse.error("生成钱包失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的区块链列表
     */
    @GetMapping("/supported-blockchains")
    public ApiResponse<List<String>> getSupportedBlockchains() {
        List<String> blockchains = Arrays.asList("BTC", "ETH", "SOL");
        return ApiResponse.success("获取成功", blockchains);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("钱包服务运行正常", "OK");
    }
}

