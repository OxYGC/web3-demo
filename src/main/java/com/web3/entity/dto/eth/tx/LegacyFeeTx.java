package com.web3.entity.dto.eth.tx;

import lombok.Data;

@Data
public class LegacyFeeTx {
    private String chainId;
    private long nonce;
    private String fromAddress;
    private String toAddress;
    private long gasLimit;
    private long gasPrice;
    private String amount;
    private String contractAddress;
}