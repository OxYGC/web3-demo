package com.web3.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class VanityGenerationTask {
    private String taskId;
    private String pattern;
    private String prefixPattern; // 新增：前缀规则
    private String suffixPattern; // 新增：后缀规则
    private String addressFormat; // 新增：地址格式
    private String coinType;
    private int maxResults;
    private String matchType;
    private String generationType;
    private String accountId;
    private String mnemonic;

    private VanityTaskStatus status;
    private int progress; // 0-100
    private AtomicInteger foundCount;
    private AtomicLong attemptCount;
    private AtomicBoolean running;

    private long estimatedTimeMs; // 预估时间
    private long runningTimeMs;   // 已运行时间
    private double currentSpeed;   // 当前速度 (尝试次数/秒)

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private List<VanityAddressResult> results = new CopyOnWriteArrayList<>();
    private String errorMessage;

    public VanityGenerationTask() {
        this.foundCount = new AtomicInteger(0);
        this.attemptCount = new AtomicLong(0);
        this.running = new AtomicBoolean(false);
    }
}
