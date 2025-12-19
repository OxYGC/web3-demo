package com.web3.scanner;

import com.web3.scanner.config.ChainConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.List;


/**
 *
 * 1,获取最新的块(eth_getBlockByNumber(....true)),然后用返回值里面的transactions的List
 * 2,用eth_getTransactionReceipt返回的status=1的，list循环进入不同的业务处理(充值提现等等)
 * 3,list循环，我需要在内存里面list.stream流处理成Map然后判断key是否是我平台的地址
 */
public abstract class AbstractBlockScanner {

    protected final ChainConfig chainConfig;
    protected final Web3j web3j;

    @Autowired
    protected BlockProgressService blockProgressService;



    // 模板方法：统一扫链流程
    public final void startSync() {
        Long currentBlock = getCurrentBlockNumber();
        Long lastProcessed = getLastProcessedBlockFromDB(chainConfig.getChainId());
        Long startBlock = (lastProcessed == null) ? currentBlock : lastProcessed + 1;
        scanBlocks(startBlock, currentBlock);
    }

    // 通用处理流程（含 confirm/reorg 检测）
    private void scanBlocks(Long from, Long to) {
        //获取确认数
        for (Long blockNumber = from; blockNumber <= to - chainConfig.getRequiredConfirmations(); blockNumber++) {
            List<ChainEvent> events = extractEventsFromBlock(blockNumber);
            for (ChainEvent event : events) {
                processEvent(event);
            }
            updateLastProcessedBlock(chainConfig.getChainId(), blockNumber);
        }
    }



    // 抽象方法：由子类决定如何提取事件
    protected abstract List<ChainEvent> extractEventsFromBlock(Long blockNumber);


    //从 DB 获取最后处理块
//    protected abstract Long getLastProcessedBlockFromDB(Long chainId);
//    从 DB 获取最后处理块
    protected Long getLastProcessedBlockFromDB(Long chainId) {
        return blockProgressService.getLastProcessedBlock(chainConfig.getChainId());
    }



    // todo 由子类实现如何更新进度
    protected abstract void updateLastProcessedBlock(Long chainId,Long blockNumber);



    public AbstractBlockScanner(ChainConfig config) {
        this.chainConfig = config;
        this.web3j = Web3j.build(new HttpService(config.getRpcUrl()));
    }





    // 事件分发（可对接不同业务处理器）
    private void processEvent(ChainEvent event) {
        EventDispatcher.dispatch(event); // 策略模式：DepositHandler, WithdrawHandler...
    }

    // 获取当前块高
    protected Long getCurrentBlockNumber() {
        // 模拟：从进度服务读取当前链头（无需真实 RPC）
        return blockProgressService.getCurrentHead(chainConfig.getChainId());
    }

//    protected TransactionReceipt getReceipt(String txHash) {
//        /* 调用 eth_getTransactionReceipt */
//        return new TransactionReceipt();
//    }

}