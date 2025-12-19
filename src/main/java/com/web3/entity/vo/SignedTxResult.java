package com.web3.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易签名返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedTxResult {
    private int code;          // 返回码
    private String message;    // 信息
    private String txHash;     // 交易 Hash
    private String signedTx;   // 签名后的交易体（可直接广播）
    private String txMessageHash; // 原始交易 Hash，用于验证
}