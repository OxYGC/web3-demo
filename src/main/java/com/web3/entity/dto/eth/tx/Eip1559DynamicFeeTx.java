package com.web3.entity.dto.eth.tx;

import lombok.Data;

@Data
public class Eip1559DynamicFeeTx {
    private String chainId;
    private long nonce;
    private String fromAddress;
    private String toAddress;
    private long gasLimit;
    private long gas; // 可选字段
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String amount;
    private String contractAddress;
}