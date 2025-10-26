package com.web3.service;

import com.web3.dto.VanityAddressResult;
import com.web3.dto.VanityGenerationTask;
import com.web3.dto.VanityTaskStatus;
import com.web3.entity.VanityAddress;
import com.web3.repository.VanityAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class VanityAddressService {

    static {
        System.out.println("VanityAddressService class loaded");
    }

    public VanityAddressService(WalletService walletService, VanityAddressRepository vanityAddressRepository, 
                              VanityTaskManager taskManager, AccountService accountService) {
        this.walletService = walletService;
        this.vanityAddressRepository = vanityAddressRepository;
        this.taskManager = taskManager;
        this.accountService = accountService;
        System.out.println("VanityAddressService initialized with dependencies");
    }

    private final WalletService walletService;
    private final VanityAddressRepository vanityAddressRepository;
    private final VanityTaskManager taskManager;
    private final AccountService accountService;

    /**
     * 创建靓号生成任务
     */
    public String createVanityAddressTask(String pattern, String coinType, int maxResults, 
                                         String matchType, String generationType, 
                                         String accountId, String mnemonic) {
        // 验证参数
        validateGenerationParameters(generationType, accountId, mnemonic);

        // 创建任务
        String taskId = taskManager.createTask(pattern, coinType, maxResults, matchType, 
                                             generationType, accountId, mnemonic);

        log.info("创建靓号生成任务: {}", taskId);
        return taskId;
    }

    /**
     * 开始执行靓号生成任务
     */
    public void startVanityAddressGeneration(String taskId) {
        VanityGenerationTask task = taskManager.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        taskManager.startTask(taskId);

        // 异步执行生成任务
        CompletableFuture.runAsync(() -> executeGenerationTask(task), taskManager.getVirtualThreadExecutor());
    }

    /**
     * 执行生成任务的核心逻辑
     */
    private void executeGenerationTask(VanityGenerationTask task) {
        try {
            log.info("开始执行靓号生成任务 - 模式: {}, 币种: {}, 数量: {}, 匹配类型: {}", 
                    task.getPattern(), task.getCoinType(), task.getMaxResults(), task.getMatchType());

            // 验证生成方式和参数
            String baseMnemonic = null;
            if ("mnemonic".equals(task.getGenerationType())) {
                baseMnemonic = task.getMnemonic().trim();
                log.info("使用助记词生成靓号地址");
            } else if ("account".equals(task.getGenerationType())) {
                log.info("使用账号 {} 生成靓号地址", task.getAccountId());
            }

            AtomicInteger found = new AtomicInteger(0);
            AtomicLong attempts = new AtomicLong(0);
            List<VanityAddressResult> results = new ArrayList<>();

            // 使用虚拟线程并行生成
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            final String finalMnemonic = baseMnemonic;
            int threadCount = Math.min(50, task.getMaxResults() * 10); // 增加线程数，利用虚拟线程优势

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    while (found.get() < task.getMaxResults() && task.getRunning().get()) {
                        try {
                            long genStartTime = System.currentTimeMillis();
                            String privateKey;
                            List<String> blockchains = List.of(task.getCoinType());
                            List<com.web3.dto.WalletInfo> wallets;

                            if ("mnemonic".equals(task.getGenerationType()) && finalMnemonic != null) {
                                // 使用助记词生成，每次使用不同的索引
                                int derivationIndex = threadIndex * 10000 + (int)attempts.get();
                                wallets = walletService.generateWalletsFromMnemonic(finalMnemonic, blockchains, 1, derivationIndex);
                                privateKey = wallets.isEmpty() ? null : wallets.get(0).getPrivateKey();
                            } else {
                                // 使用随机私钥生成
                                privateKey = walletService.generatePrivateKey();
                                wallets = walletService.generateWalletsFromPrivateKey(privateKey, blockchains);
                            }

                            attempts.incrementAndGet();

                            // 定期更新进度
                            if (attempts.get() % 100 == 0) {
                                taskManager.updateProgress(task.getTaskId(), found.get(), attempts.get());
                            }

                            if (!wallets.isEmpty() && wallets.get(0) != null) {
                                String address = wallets.get(0).getAddress();
                                if (matchesPattern(address, task.getPattern(), task.getMatchType())) {
                                    long genTime = System.currentTimeMillis() - genStartTime;

                                    synchronized (results) {
                                        if (found.get() < task.getMaxResults()) {
                                            VanityAddressResult result = new VanityAddressResult(
                                                address,
                                                privateKey,
                                                wallets.get(0).getPublicKey(),
                                                task.getCoinType(),
                                                task.getPattern()
                                            );
                                            results.add(result);
                                            task.getResults().add(result);

                                            // 持久化到数据库，关联账户
                                            VanityAddress entity = new VanityAddress(
                                                address,
                                                privateKey,
                                                wallets.get(0).getPublicKey(),
                                                task.getCoinType(),
                                                task.getPattern(),
                                                task.getMatchType(),
                                                genTime,
                                                task.getAccountId(),
                                                task.getTaskId()
                                            );
                                            vanityAddressRepository.save(entity);

                                            // 如果有账户ID，将地址关联到账户
                                            if (task.getAccountId() != null && !task.getAccountId().trim().isEmpty()) {
                                                try {
                                                    accountService.addVanityAddressToAccount(task.getAccountId(), entity);
                                                } catch (Exception e) {
                                                    log.warn("关联靓号地址到账户失败: {}", e.getMessage());
                                                }
                                            }

                                            found.incrementAndGet();
                                            taskManager.updateProgress(task.getTaskId(), found.get(), attempts.get());

                                            log.info("找到匹配地址 {}/{}: {} (尝试次数: {})", 
                                                   found.get(), task.getMaxResults(), address, attempts.get());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("生成地址时出现异常: ", e);
                        }
                    }
                }, taskManager.getVirtualThreadExecutor());

                futures.add(future);
            }

            // 等待所有任务完成或被停止
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 任务完成
            taskManager.completeTask(task.getTaskId());

            log.info("靓号地址生成任务完成 - 任务ID: {}, 总尝试次数: {}, 成功数量: {}", 
                    task.getTaskId(), attempts.get(), results.size());

        } catch (Exception e) {
            log.error("执行靓号生成任务失败: {}", task.getTaskId(), e);
            task.setStatus(VanityTaskStatus.ERROR);
            task.setErrorMessage(e.getMessage());
        }
    }

    /**
     * 验证生成参数
     */
    private void validateGenerationParameters(String generationType, String accountId, String mnemonic) {
        if ("mnemonic".equals(generationType)) {
            if (mnemonic == null || mnemonic.trim().isEmpty()) {
                throw new IllegalArgumentException("使用助记词生成时，助记词不能为空");
            }
            if (!walletService.validateMnemonic(mnemonic.trim())) {
                throw new IllegalArgumentException("助记词格式不正确");
            }
        } else if ("account".equals(generationType)) {
            if (accountId == null || accountId.trim().isEmpty()) {
                throw new IllegalArgumentException("使用账号生成时，必须选择账号");
            }
            // 验证账号是否存在
            if (!accountService.accountExists(accountId)) {
                throw new IllegalArgumentException("账号不存在: " + accountId);
            }
        }
    }

    /**
     * 生成靓号地址并持久化 (保持兼容性)
     */
    @Transactional
    public List<VanityAddressResult> generateVanityAddresses(String pattern, String coinType, int maxResults, 
                                                           String matchType, String generationType, 
                                                           String accountId, String mnemonic) {
        // 创建并执行任务
        String taskId = createVanityAddressTask(pattern, coinType, maxResults, matchType, 
                                               generationType, accountId, mnemonic);
        startVanityAddressGeneration(taskId);

        // 等待任务完成
        VanityGenerationTask task = taskManager.getTask(taskId);
        while (task.getStatus() == VanityTaskStatus.RUNNING || task.getStatus() == VanityTaskStatus.PENDING) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return new ArrayList<>(task.getResults());
    }

    /**
     * 计算预估生成时间
     */
    public Map<String, Object> calculateEstimatedTime(String pattern, String matchType, int maxResults) {
        Map<String, Object> result = new HashMap<>();

        if (pattern == null || pattern.trim().isEmpty()) {
            result.put("estimatedTimeMs", 1000L);
            result.put("estimatedTimeText", "约 1 秒");
            result.put("difficulty", "简单");
            return result;
        }

        int patternLength = pattern.length();
        long baseTime;
        String difficulty;

        switch (matchType.toUpperCase()) {
            case "PREFIX":
            case "SUFFIX":
                baseTime = (long) Math.pow(16, patternLength) / 2000; // 考虑虚拟线程优化
                difficulty = patternLength <= 3 ? "简单" : patternLength <= 5 ? "中等" : "困难";
                break;
            case "CONTAINS":
            default:
                baseTime = (long) Math.pow(16, patternLength) / 4000; // 包含匹配相对容易
                difficulty = patternLength <= 4 ? "简单" : patternLength <= 6 ? "中等" : "困难";
                break;
        }

        long estimatedTime = Math.max(1000, Math.min(baseTime * maxResults, 30 * 60 * 1000));

        result.put("estimatedTimeMs", estimatedTime);
        result.put("estimatedTimeText", formatTime(estimatedTime));
        result.put("difficulty", difficulty);
        result.put("patternLength", patternLength);

        return result;
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return "约 1 秒";
        } else if (milliseconds < 60 * 1000) {
            return String.format("约 %d 秒", milliseconds / 1000);
        } else if (milliseconds < 60 * 60 * 1000) {
            return String.format("约 %d 分钟", milliseconds / (60 * 1000));
        } else {
            return String.format("约 %d 小时", milliseconds / (60 * 60 * 1000));
        }
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
