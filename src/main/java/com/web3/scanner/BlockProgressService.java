package com.web3.scanner;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 通用进度服务
@Service
public class BlockProgressService {
    // 简易内存存储，模拟数据库
    private final Map<Long, Long> lastProcessedMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> mockHeadMap = new ConcurrentHashMap<>();

    public Long getLastProcessedBlock(Long chainId) {
        return lastProcessedMap.getOrDefault(chainId, 0L);
    }

    public void updateLastProcessedBlock(Long chainId, Long blockNumber) {
        lastProcessedMap.put(chainId, blockNumber);
        // 同步推进 head，保证有块可扫（仅模拟场景）
        mockHeadMap.merge(chainId, blockNumber + 20, Math::max);
    }

    // 仅模拟：返回当前链头高度
    public Long getCurrentHead(Long chainId) {
        return mockHeadMap.getOrDefault(chainId, Math.max(20L, getLastProcessedBlock(chainId) + 20));
    }
}