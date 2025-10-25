package com.web3.entity.dto.tron;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TRON 密钥对与地址结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TronSchema {
    private String privateKey;       // 私钥 (hex)
    private String publicKey;        // 公钥
    private String compressedPubKey; // 压缩公钥
    private String address;          // Tron 地址 (Base58Check, T开头)
}