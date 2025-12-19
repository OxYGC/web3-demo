package com.web3.scanner.chain.bsc;

import com.google.common.collect.Lists;
import com.web3.scanner.AbstractBlockScanner;
import com.web3.scanner.ChainEvent;
import com.web3.scanner.config.BscChainConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BscBlockScanner extends AbstractBlockScanner {


    public BscBlockScanner(BscChainConfig config) {
        super(config);
    }

    @Override
    protected void updateLastProcessedBlock(Long chainId, Long blockNumber) {

    }

    @Override
    protected List<ChainEvent> extractEventsFromBlock(Long blockNumber) {
        // 同样监听 ETH 交易 + ERC-20 事件，但合约地址不同（如 BSC USDT: 0x55d398326f99059fF775485246999027B3197955）
        return Lists.newArrayList();
    }
}