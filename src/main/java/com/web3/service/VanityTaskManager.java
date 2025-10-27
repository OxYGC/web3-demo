package com.web3.service;

import com.web3.dto.VanityGenerationTask;
import com.web3.dto.VanityTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class VanityTaskManager {

    private final Map<String, VanityGenerationTask> activeTasks = new ConcurrentHashMap<>();
    private final java.util.concurrent.ExecutorService virtualThreadExecutor = 
            Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 创建新的生成任务
     */
    public String createTask(String pattern, String coinType, int maxResults, 
                           String matchType, String generationType, String accountId, String mnemonic,
                           String addressFormat, String prefixPattern, String suffixPattern) {
        String taskId = generateTaskId();

        VanityGenerationTask task = new VanityGenerationTask();
        task.setTaskId(taskId);
        task.setPattern(pattern);
        task.setPrefixPattern(prefixPattern);
        task.setSuffixPattern(suffixPattern);
        task.setAddressFormat(addressFormat);
        task.setCoinType(coinType);
        task.setMaxResults(maxResults);
        task.setMatchType(matchType);
        task.setGenerationType(generationType);
        task.setAccountId(accountId);
        task.setMnemonic(mnemonic);
        task.setStatus(VanityTaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setProgress(0);
        task.setFoundCount(new AtomicInteger(0));
        task.setAttemptCount(new AtomicLong(0));
        task.setRunning(new AtomicBoolean(false));
        task.setEstimatedTimeMs(calculateEstimatedTime(pattern, matchType, maxResults));

        activeTasks.put(taskId, task);
        log.info("创建靓号生成任务: {}", taskId);

        return taskId;
    }

    /**
     * 开始执行任务
     */
    public void startTask(String taskId) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        if (task.getRunning().get()) {
            throw new IllegalStateException("任务已在运行中");
        }

        task.setStatus(VanityTaskStatus.RUNNING);
        task.getRunning().set(true);
        task.setStartedAt(LocalDateTime.now());

        log.info("开始执行靓号生成任务: {}", taskId);
    }

    /**
     * 暂停任务
     */
    public void pauseTask(String taskId) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.getRunning().set(false);
        task.setStatus(VanityTaskStatus.PAUSED);

        log.info("暂停靓号生成任务: {}", taskId);
    }

    /**
     * 恢复任务
     */
    public void resumeTask(String taskId) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.getRunning().set(true);
        task.setStatus(VanityTaskStatus.RUNNING);

        log.info("恢复靓号生成任务: {}", taskId);
    }

    /**
     * 停止任务
     */
    public void stopTask(String taskId) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.getRunning().set(false);
        task.setStatus(VanityTaskStatus.STOPPED);
        task.setCompletedAt(LocalDateTime.now());

        log.info("停止靓号生成任务: {}", taskId);
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            return;
        }

        task.getRunning().set(false);
        task.setStatus(VanityTaskStatus.COMPLETED);
        task.setProgress(100);
        task.setCompletedAt(LocalDateTime.now());

        log.info("靓号生成任务完成: {}", taskId);
    }

    /**
     * 更新任务进度
     */
    public void updateProgress(String taskId, int foundCount, long attemptCount) {
        VanityGenerationTask task = activeTasks.get(taskId);
        if (task == null) {
            return;
        }

        task.getFoundCount().set(foundCount);
        task.getAttemptCount().set(attemptCount);

        // 计算进度百分比
        int progress = Math.min(100, (foundCount * 100) / task.getMaxResults());
        task.setProgress(progress);

        // 更新运行时间
        if (task.getStartedAt() != null) {
            long runningTime = java.time.Duration.between(task.getStartedAt(), LocalDateTime.now()).toMillis();
            task.setRunningTimeMs(runningTime);

            // 计算当前速度 (尝试次数/秒)
            if (runningTime > 0) {
                double speed = (attemptCount * 1000.0) / runningTime;
                task.setCurrentSpeed(speed);
            }
        }
    }

    /**
     * 获取任务状态
     */
    public VanityGenerationTask getTask(String taskId) {
        return activeTasks.get(taskId);
    }

    /**
     * 移除已完成的任务
     */
    public void removeTask(String taskId) {
        activeTasks.remove(taskId);
        log.info("移除任务: {}", taskId);
    }

    /**
     * 获取所有活跃任务
     */
    public Map<String, VanityGenerationTask> getAllActiveTasks() {
        return new ConcurrentHashMap<>(activeTasks);
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "TASK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 计算预估时间 (毫秒)
     */
    private long calculateEstimatedTime(String pattern, String matchType, int maxResults) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return 1000; // 1秒默认值
        }

        // 基础计算: 根据模式复杂度估算
        int patternLength = pattern.length();
        long baseTime;

        switch (matchType.toUpperCase()) {
            case "PREFIX":
            case "SUFFIX":
                // 前缀后缀匹配相对容易
                baseTime = (long) Math.pow(16, patternLength) / 1000; // 16进制字符
                break;
            case "CONTAINS":
            default:
                // 包含匹配稍微容易一些
                baseTime = (long) Math.pow(16, patternLength) / 2000;
                break;
        }

        // 考虑要生成的数量
        long estimatedTime = baseTime * maxResults;

        // 设置合理的范围 (最少1秒，最多30分钟)
        return Math.max(1000, Math.min(estimatedTime, 30 * 60 * 1000));
    }

    public java.util.concurrent.ExecutorService getVirtualThreadExecutor() {
        return virtualThreadExecutor;
    }
}
