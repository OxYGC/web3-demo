package com.web3.entity;

import lombok.Data;

/**
 * 签名请求 DTO
 */
@Data
public class SignReq {
    private String chainId;   // BTC, ETH, SOL, etc.
    private String keyId;     // 哪个私钥
    private String rawData;   // 待签名的交易数据

    // getters and setters
}