package com.web3.controller.ethereum;

import com.web3.dto.ApiResponse;
import com.web3.scanner.BlockProgressService;
import com.web3.scanner.ChainEvent;
import com.web3.scanner.chain.ethereum.EthereumBlockScanner;
import com.web3.scanner.config.EthereumChainConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scan/eth")
@RequiredArgsConstructor
@Slf4j
public class EthereumScanner {

    private final EthereumBlockScanner ethereumBlockScanner;
    private final BlockProgressService blockProgressService;
    private final EthereumChainConfig ethereumChainConfig;

    // 触发一次扫描（从上次进度到当前头 - confirmations）
    @PostMapping("/run")
    public ApiResponse<String> runOnce() {
        log.info("[Scanner] 手动触发以太坊扫描");
        ethereumBlockScanner.startSync();
        return ApiResponse.success("扫描已执行", "ok");
    }

    // 查询进度
    @GetMapping("/progress")
    public ApiResponse<Map<String, Object>> progress() {
        Long chainId = ethereumChainConfig.getChainId();
        Long lastProcessed = blockProgressService.getLastProcessedBlock(chainId);
        Long head = blockProgressService.getCurrentHead(chainId);
        Long confirmations = ethereumChainConfig.getRequiredConfirmations();
        Map<String, Object> data = new HashMap<>();
        data.put("chainId", chainId);
        data.put("lastProcessed", lastProcessed);
        data.put("head", head);
        data.put("confirmations", confirmations);
        data.put("scanTo", Math.max(0, head - confirmations));
        return ApiResponse.success("进度获取成功", data);
    }

    // 预览某个区块的模拟事件
    @GetMapping("/block/{number}")
    public ApiResponse<List<ChainEvent>> preview(@PathVariable("number") Long blockNumber) {
        List<ChainEvent> events = ethereumBlockScanner.previewEvents(blockNumber);
        return ApiResponse.success("区块事件预览", events);
    }
}
