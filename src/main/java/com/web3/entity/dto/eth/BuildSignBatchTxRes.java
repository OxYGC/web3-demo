package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量交易签名响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildSignBatchTxRes {
    private int code;                             // 状态码，0 表示成功
    private String message;                        // 状态信息
    private List<SignedTxResultDTO> signedTxList; // 批量交易签名结果列表
}