package com.web3.scanner.chain.ethereum;

import com.google.common.collect.Lists;
import com.web3.scanner.AbstractBlockScanner;
import com.web3.scanner.ChainEvent;
import com.web3.scanner.config.EthereumChainConfig;
import org.springframework.stereotype.Service;

import java.util.List;

// Ethereum 扫描器
@Service
public class EthereumBlockScanner extends AbstractBlockScanner {
    public EthereumBlockScanner(EthereumChainConfig config) {
        super(config);
    }

    @Override
    protected List<ChainEvent> extractEventsFromBlock(Long blockNumber) {
        // 1. 调用 eth_getBlockByNumber 获取 ETH 交易
        // 2. 调用 eth_getLogs 监听 USDT/USDC 等合约 Transfer 事件

        // 3. 返回统一 ChainEvent 列表

        return Lists.newArrayList();
    }

    @Override
    protected void updateLastProcessedBlock(Long chainId, Long blockNumber) {

    }


}

// BSC 扫描器（复用 AbstractBlockScanner 90% 逻辑）
