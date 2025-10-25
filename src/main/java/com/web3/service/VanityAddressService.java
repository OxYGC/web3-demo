package com.web3.service;

import com.web3.dto.VanityAddressResult;
import com.web3.entity.VanityAddress;
import com.web3.repository.VanityAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class VanityAddressService {

    static {
        System.out.println("VanityAddressService class loaded");
    }

    public VanityAddressService(WalletService walletService, VanityAddressRepository vanityAddressRepository) {
        this.walletService = walletService;
        this.vanityAddressRepository = vanityAddressRepository;
        System.out.println("VanityAddressService initialized with dependencies");
    }

    private final WalletService walletService;
    private final VanityAddressRepository vanityAddressRepository;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 生成靓号地址并持久化
     */
    @Transactional
    public List<VanityAddressResult> generateVanityAddresses(String pattern, String coinType, int maxResults, 
                                                           String matchType, String generationType, 
                                                           String accountId, String mnemonic) {
        long startTime = System.currentTimeMillis();
        List<VanityAddressResult> results = new ArrayList<>();
        AtomicInteger found = new AtomicInteger(0);
        AtomicInteger attempts = new AtomicInteger(0);

        log.info("开始生成靓号地址 - 模式: {}, 币种: {}, 数量: {}, 匹配类型: {}", pattern, coinType, maxResults, matchType);

        // 验证生成方式和参数
        String baseMnemonic = null;
        if ("mnemonic".equals(generationType)) {
            if (mnemonic == null || mnemonic.trim().isEmpty()) {
                throw new IllegalArgumentException("使用助记词生成时，助记词不能为空");
            }
            if (!walletService.validateMnemonic(mnemonic.trim())) {
                throw new IllegalArgumentException("助记词格式不正确");
            }
            baseMnemonic = mnemonic.trim();
            log.info("使用助记词生成靓号地址");
        } else if ("account".equals(generationType)) {
            if (accountId == null || accountId.trim().isEmpty()) {
                throw new IllegalArgumentException("使用账号生成时，必须选择账号");
            }
            // 这里可以添加账号验证逻辑
            log.info("使用账号 {} 生成靓号地址", accountId);
        }

        // 使用虚拟线程并行生成
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        final String finalMnemonic = baseMnemonic;

        for (int i = 0; i < Math.min(20, maxResults * 5); i++) { // 动态调整线程数
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                while (found.get() < maxResults) {
                    try {
                        long genStartTime = System.currentTimeMillis();
                        String privateKey;
                        List<String> blockchains = List.of(coinType);
                        List<com.web3.dto.WalletInfo> wallets;

                        if ("mnemonic".equals(generationType) && finalMnemonic != null) {
                            // 使用助记词生成，每次使用不同的索引
                            int derivationIndex = threadIndex * 1000 + attempts.get();
                            wallets = walletService.generateWalletsFromMnemonic(finalMnemonic, blockchains, 1, derivationIndex);
                            privateKey = wallets.isEmpty() ? null : wallets.get(0).getPrivateKey();
                        } else {
                            // 使用随机私钥生成
                            privateKey = walletService.generatePrivateKey();
                            wallets = walletService.generateWalletsFromPrivateKey(privateKey, blockchains);
                        }

                        attempts.incrementAndGet();

                        if (!wallets.isEmpty() && wallets.get(0) != null) {
                            String address = wallets.get(0).getAddress();
                            if (matchesPattern(address, pattern, matchType)) {
                                long genTime = System.currentTimeMillis() - genStartTime;

                                synchronized (results) {
                                    if (found.get() < maxResults) {
                                        VanityAddressResult result = new VanityAddressResult(
                                            address,
                                            privateKey,
                                            wallets.get(0).getPublicKey(),
                                            coinType,
                                            pattern
                                        );
                                        results.add(result);

                                        // 持久化到数据库
                                        VanityAddress entity = new VanityAddress(
                                            address,
                                            privateKey,
                                            wallets.get(0).getPublicKey(),
                                            coinType,
                                            pattern,
                                            matchType,
                                            genTime
                                        );
                                        vanityAddressRepository.save(entity);

                                        found.incrementAndGet();
                                        log.info("找到匹配地址 {}/{}: {} (尝试次数: {})", 
                                               found.get(), maxResults, address, attempts.get());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("生成地址时出现异常: ", e);
                    }
                }
            }, virtualThreadExecutor);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("靓号地址生成完成 - 总时间: {}ms, 总尝试次数: {}, 成功数量: {}", 
                totalTime, attempts.get(), results.size());

        return results;
    }

    /**
     * 获取所有已生成的靓号地址
     */
    public List<VanityAddress> getAllVanityAddresses() {
        log.info("获取所有靓号地址");
        try {
            List<VanityAddress> addresses = vanityAddressRepository.findAllOrderByCreatedAtDesc();
            log.info("找到 {} 个靓号地址", addresses.size());
            return addresses;
        } catch (Exception e) {
            log.error("获取靓号地址失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据币种获取靓号地址
     */
    public List<VanityAddress> getVanityAddressesByCoinType(String coinType) {
        log.info("根据币种获取靓号地址: {}", coinType);
        try {
            List<VanityAddress> addresses = vanityAddressRepository.findByCoinTypeOrderByCreatedAtDesc(coinType);
            log.info("找到 {} 个 {} 靓号地址", addresses.size(), coinType);
            return addresses;
        } catch (Exception e) {
            log.error("根据币种获取靓号地址失败: {}", coinType, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取靓号地址统计信息
     */
    public Map<String, Long> getVanityAddressStats() {
        log.info("获取靓号地址统计信息");
        Map<String, Long> stats = new HashMap<>();

        try {
            long totalCount = vanityAddressRepository.count();
            long btcCount = vanityAddressRepository.countByCoinType("BTC");
            long ethCount = vanityAddressRepository.countByCoinType("ETH");
            long solCount = vanityAddressRepository.countByCoinType("SOL");

            stats.put("total", totalCount);
            stats.put("BTC", btcCount);
            stats.put("ETH", ethCount);
            stats.put("SOL", solCount);

            log.info("靓号地址统计: total={}, BTC={}, ETH={}, SOL={}", totalCount, btcCount, ethCount, solCount);
        } catch (Exception e) {
            log.error("获取靓号地址统计失败", e);
        }

        return stats;
    }

    /**
     * 检查地址是否匹配模式
     */
    private boolean matchesPattern(String address, String pattern, String matchType) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }

        String cleanAddress = address.toLowerCase();
        String cleanPattern = pattern.toLowerCase();

        // 移除地址前缀（如0x、1等）进行匹配
        String addressForMatch = cleanAddress;
        if (cleanAddress.startsWith("0x")) {
            addressForMatch = cleanAddress.substring(2);
        } else if (cleanAddress.startsWith("1") || cleanAddress.startsWith("3") || cleanAddress.startsWith("bc1")) {
            // Bitcoin地址保持原样
        }

        return switch (matchType.toUpperCase()) {
            case "PREFIX" -> addressForMatch.startsWith(cleanPattern);
            case "SUFFIX" -> addressForMatch.endsWith(cleanPattern);
            case "CONTAINS" -> addressForMatch.contains(cleanPattern);
            default -> addressForMatch.contains(cleanPattern);
        };
    }

    /**
     * 生成下载文件内容
     */
    public String generateDownloadContent(List<VanityAddressResult> results) {
        StringBuilder content = new StringBuilder();
        content.append("🚀 Web3 靓号地址生成结果\n");
        content.append("⏰ 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        content.append("📊 总数量: ").append(results.size()).append("\n");
        content.append("=====================================\n\n");

        for (int i = 0; i < results.size(); i++) {
            VanityAddressResult result = results.get(i);
            content.append("🎯 序号: #").append(i + 1).append("\n");
            content.append("💎 币种: ").append(result.getCoinType()).append("\n");
            content.append("🏠 地址: ").append(result.getAddress()).append("\n");
            content.append("🔐 私钥: ").append(result.getPrivateKey()).append("\n");
            content.append("🔑 公钥: ").append(result.getPublicKey()).append("\n");
            content.append("🎨 匹配模式: ").append(result.getPattern()).append("\n");
            content.append("-------------------------------------\n");
        }

        content.append("\n⚠️ 安全提示: 请妥善保管您的私钥，不要泄露给任何人！\n");
        content.append("🌐 Generated by Web3 Wallet System\n");

        return content.toString();
    }
}
