package com.web3.scanner;

import lombok.Data;

import java.math.BigInteger;

// 统一事件模型
@Data
public class ChainEvent {
    private Long chainId;
    private String eventType; // DEPOSIT, WITHDRAW, COLLECT
    private String token;
    private String toAddress;
    private BigInteger rawAmount;
    private String txHash;
    // ...
}

