package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建单个密钥对响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKeyPairResponse {
    private int code;                  // 返回码
    private String message;            // 返回信息
//    private String privateKey;         // 私钥 hex
    private String publicKey;          // 公钥 hex
    private String compressedPubKey;   // 压缩公钥 hex
    private String address;            // 地址（可选，ETH/BTC可生成）
}