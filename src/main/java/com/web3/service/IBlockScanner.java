package com.web3.service;


public interface IBlockScanner {


    Long requiredConfirmNum(Long chainId);

    /**
     * 开始同步扫链
     */
     Boolean startSync();

    Boolean scanAndProcess(Long currentBlock);

    Boolean depositProcess(Long currentBlock);

    Boolean contractPocess(Long currentBlock);

}
