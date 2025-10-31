package com.web3.scanner;

import org.springframework.stereotype.Service;

// 通用进度服务
@Service
public class BlockProgressService {

    public Long getLastProcessedBlock(Long chainId) {

//        todo 数据库查询
//        return blockProgressRepo.findByChainId(chainId);
        return 0L;
    }

    public void updateLastProcessedBlock(Long chainId, Long blockNumber) {
          //
//        blockProgressRepo.upsert(chainId, blockNumber);

    }
}