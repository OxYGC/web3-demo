package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回签名机支持的签名算法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChainSignMethodResponse {
    private int code;          // 返回码，0=成功，非0=失败
    private String message;    // 返回信息
    private String signMethod; // 签名算法，如 "ecdsa"
}