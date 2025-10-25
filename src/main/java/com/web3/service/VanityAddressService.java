package com.web3.service;

import com.web3.dto.VanityAddressResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class VanityAddressService {

    private final WalletService walletService;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 生成靓号地址
     */
    public List<VanityAddressResult> generateVanityAddresses(String pattern, String coinType, int maxResults) {
        List<VanityAddressResult> results = new ArrayList<>();
        AtomicInteger found = new AtomicInteger(0);

        // 使用虚拟线程并行生成
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) { // 启动20个并行任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                while (found.get() < maxResults) {
                    try {
                        String privateKey = walletService.generatePrivateKey();
                        List<String> blockchains = List.of(coinType);
                        var wallets = walletService.generateWalletsFromPrivateKey(privateKey, blockchains);

                        if (!wallets.isEmpty() && wallets.get(0) != null) {
                            String address = wallets.get(0).getAddress();
                            if (matchesPattern(address, pattern)) {
                                synchronized (results) {
                                    if (found.get() < maxResults) {
                                        results.add(new VanityAddressResult(
                                            address,
                                            privateKey,
                                            wallets.get(0).getPublicKey(),
                                            coinType,
                                            pattern
                                        ));
                                        found.incrementAndGet();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略异常，继续尝试
                    }
                }
            }, virtualThreadExecutor);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return results;
    }

    /**
     * 检查地址是否匹配模式
     */
    private boolean matchesPattern(String address, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }

        String cleanAddress = address.toLowerCase();
        String cleanPattern = pattern.toLowerCase();

        // 支持多种匹配模式
        return cleanAddress.contains(cleanPattern) || 
               cleanAddress.startsWith(cleanPattern) || 
               cleanAddress.endsWith(cleanPattern);
    }

    /**
     * 生成下载文件内容
     */
    public String generateDownloadContent(List<VanityAddressResult> results) {
        StringBuilder content = new StringBuilder();
        content.append("靓号地址生成结果\n");
        content.append("生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        content.append("总数量: ").append(results.size()).append("\n");
        content.append("=====================================\n\n");

        for (int i = 0; i < results.size(); i++) {
            VanityAddressResult result = results.get(i);
            content.append("序号: ").append(i + 1).append("\n");
            content.append("币种: ").append(result.getCoinType()).append("\n");
            content.append("地址: ").append(result.getAddress()).append("\n");
            content.append("私钥: ").append(result.getPrivateKey()).append("\n");
            content.append("公钥: ").append(result.getPublicKey()).append("\n");
            content.append("匹配模式: ").append(result.getPattern()).append("\n");
            content.append("-------------------------------------\n");
        }

        return content.toString();
    }
}
