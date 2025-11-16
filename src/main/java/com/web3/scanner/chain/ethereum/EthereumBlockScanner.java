package com.web3.scanner.chain.ethereum;

import com.google.common.collect.Lists;
import com.web3.scanner.AbstractBlockScanner;
import com.web3.scanner.ChainEvent;
import com.web3.scanner.config.EthereumChainConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.math.BigInteger;

// Ethereum 扫描器
@Service
public class EthereumBlockScanner extends AbstractBlockScanner {
    public EthereumBlockScanner(EthereumChainConfig config) {
        super(config);
    }

    @Override
    protected List<ChainEvent> extractEventsFromBlock(Long blockNumber) {
        // 模拟：根据区块号生成确定性的交易事件，避免纯随机导致不可复现
        Random r = new Random(blockNumber);
        List<ChainEvent> list = Lists.newArrayList();

        // 模拟生成 0~3 笔 ETH 转账
        int ethTxCount = r.nextInt(4);
        for (int i = 0; i < ethTxCount; i++) {
            ChainEvent e = new ChainEvent();
            e.setChainId(((EthereumChainConfig) this.chainConfig).getChainId());
            e.setEventType("DEPOSIT");
            e.setToken("ETH");
            e.setToAddress(mockAddress(r));
            e.setRawAmount(BigInteger.valueOf(10_000_000_000L).multiply(BigInteger.valueOf(r.nextInt(100) + 1L))); // 0.01~1 ETH(以 wei 表示近似)
            e.setTxHash(mockTxHash(blockNumber, i, "ETH"));
            list.add(e);
        }

        // 模拟生成 0~2 笔 ERC20(USDT) 转账
        int erc20TxCount = r.nextInt(3);
        for (int i = 0; i < erc20TxCount; i++) {
            ChainEvent e = new ChainEvent();
            e.setChainId(((EthereumChainConfig) this.chainConfig).getChainId());
            e.setEventType("DEPOSIT");
            e.setToken("USDT");
            e.setToAddress(mockAddress(r));
            // USDT 6 位精度，这里直接给原始整数数量
            e.setRawAmount(BigInteger.valueOf(100_000L).multiply(BigInteger.valueOf(r.nextInt(500) + 1L))); // 1~500 USDT（放大 1e6 的整数）
            e.setTxHash(mockTxHash(blockNumber, i, "USDT"));
            list.add(e);
        }

        return list;
    }

    @Override
    protected void updateLastProcessedBlock(Long chainId, Long blockNumber) {
        // 写入通用进度服务（父类已注入）
        blockProgressService.updateLastProcessedBlock(chainId, blockNumber);
    }

    // 提供一个公共预览方法，便于 Controller 调用
    public List<ChainEvent> previewEvents(Long blockNumber) {
        return extractEventsFromBlock(blockNumber);
    }

    private static String mockTxHash(Long blockNumber, int idx, String tag) {
        return String.format("0x%08x%02x%s%02x", blockNumber, idx, Integer.toHexString(tag.hashCode()), (blockNumber + idx) & 0xff);
    }

    private static String mockAddress(Random r) {
        // 生成一个 20 字节的地址十六进制
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < 20; i++) {
            sb.append(String.format("%02x", r.nextInt(256)));
        }
        return sb.toString();
    }
}

// BSC 扫描器（复用 AbstractBlockScanner 90% 逻辑）
