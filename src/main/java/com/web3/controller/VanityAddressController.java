package com.web3.controller;

import com.web3.dto.ApiResponse;
import com.web3.dto.VanityAddressRequest;
import com.web3.dto.VanityAddressResult;
import com.web3.service.VanityAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vanity")
@RequiredArgsConstructor
public class VanityAddressController {

    private final VanityAddressService vanityAddressService;
    private final com.web3.service.VanityTaskManager taskManager;

    /**
     * 预估生成时间
     */
    @PostMapping("/estimate")
    public ApiResponse<Map<String, Object>> estimateGenerationTime(@RequestBody Map<String, Object> request) {
        try {
            String pattern = (String) request.get("pattern");
            String matchType = (String) request.getOrDefault("matchType", "CONTAINS");
            Integer maxResults = (Integer) request.getOrDefault("maxResults", 1);

            Map<String, Object> estimation = vanityAddressService.calculateEstimatedTime(pattern, matchType, maxResults);
            return ApiResponse.success("预估时间计算成功", estimation);
        } catch (Exception e) {
            log.error("预估时间计算失败", e);
            return ApiResponse.error("预估时间计算失败: " + e.getMessage());
        }
    }

    /**
     * 创建靓号生成任务
     */
    @PostMapping("/create-task")
    public ApiResponse<String> createVanityTask(@Valid @RequestBody VanityAddressRequest request) {
        try {
            String taskId = vanityAddressService.createVanityAddressTask(
                    request.getPattern(),
                    request.getCoinType(),
                    request.getMaxResults(),
                    request.getMatchType(),
                    request.getGenerationType(),
                    request.getAccountId(),
                    request.getMnemonic()
            );

            return ApiResponse.success("任务创建成功", taskId);
        } catch (Exception e) {
            log.error("创建靓号生成任务失败", e);
            return ApiResponse.error("创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 开始执行任务
     */
    @PostMapping("/start-task/{taskId}")
    public ApiResponse<String> startTask(@PathVariable String taskId) {
        try {
            vanityAddressService.startVanityAddressGeneration(taskId);
            return ApiResponse.success("任务已开始执行", taskId);
        } catch (Exception e) {
            log.error("启动任务失败: {}", taskId, e);
            return ApiResponse.error("启动任务失败: " + e.getMessage());
        }
    }

    /**
     * 暂停任务
     */
    @PostMapping("/pause-task/{taskId}")
    public ApiResponse<String> pauseTask(@PathVariable String taskId) {
        try {
            taskManager.pauseTask(taskId);
            return ApiResponse.success("任务已暂停", taskId);
        } catch (Exception e) {
            log.error("暂停任务失败: {}", taskId, e);
            return ApiResponse.error("暂停任务失败: " + e.getMessage());
        }
    }

    /**
     * 恢复任务
     */
    @PostMapping("/resume-task/{taskId}")
    public ApiResponse<String> resumeTask(@PathVariable String taskId) {
        try {
            taskManager.resumeTask(taskId);
            return ApiResponse.success("任务已恢复", taskId);
        } catch (Exception e) {
            log.error("恢复任务失败: {}", taskId, e);
            return ApiResponse.error("恢复任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/stop-task/{taskId}")
    public ApiResponse<String> stopTask(@PathVariable String taskId) {
        try {
            taskManager.stopTask(taskId);
            return ApiResponse.success("任务已停止", taskId);
        } catch (Exception e) {
            log.error("停止任务失败: {}", taskId, e);
            return ApiResponse.error("停止任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/task-status/{taskId}")
    public ApiResponse<com.web3.dto.VanityGenerationTask> getTaskStatus(@PathVariable String taskId) {
        try {
            com.web3.dto.VanityGenerationTask task = taskManager.getTask(taskId);
            if (task == null) {
                return ApiResponse.error("任务不存在");
            }
            return ApiResponse.success("获取任务状态成功", task);
        } catch (Exception e) {
            log.error("获取任务状态失败: {}", taskId, e);
            return ApiResponse.error("获取任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有活跃任务
     */
    @GetMapping("/active-tasks")
    public ApiResponse<Map<String, com.web3.dto.VanityGenerationTask>> getActiveTasks() {
        try {
            Map<String, com.web3.dto.VanityGenerationTask> tasks = taskManager.getAllActiveTasks();
            return ApiResponse.success("获取活跃任务成功", tasks);
        } catch (Exception e) {
            log.error("获取活跃任务失败", e);
            return ApiResponse.error("获取活跃任务失败: " + e.getMessage());
        }
    }

    /**
     * 生成靓号地址 (兼容旧接口)
     */
    @PostMapping("/generate")
    public ApiResponse<List<VanityAddressResult>> generateVanityAddresses(@Valid @RequestBody VanityAddressRequest request) {
        try {
            List<VanityAddressResult> results = vanityAddressService.generateVanityAddresses(
                    request.getPattern(),
                    request.getCoinType(),
                    request.getMaxResults(),
                    request.getMatchType(),
                    request.getGenerationType(),
                    request.getAccountId(),
                    request.getMnemonic()
            );

            if (results.isEmpty()) {
                return ApiResponse.error("未找到符合条件的靓号地址，请尝试修改规则");
            }

            return ApiResponse.success("靓号地址生成成功", results);
        } catch (Exception e) {
            return ApiResponse.error("生成靓号地址失败: " + e.getMessage());
        }
    }

    /**
     * 下载靓号地址文件
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadVanityAddresses(@RequestBody List<VanityAddressResult> results) {
        try {
            String content = vanityAddressService.generateDownloadContent(results);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            String filename = "vanity_addresses_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".txt";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取靓号地址统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getVanityAddressStats() {
        log.info("获取靓号地址统计信息");

        try {
            Map<String, Long> stats = vanityAddressService.getVanityAddressStats();
            return ApiResponse.success("获取统计信息成功", stats);
        } catch (Exception e) {
            log.error("获取靓号地址统计失败", e);
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ApiResponse<String> test() {
        log.info("VanityAddressController test endpoint called");
        return ApiResponse.success("VanityAddressController test successful", "TEST_OK");
    }
}
