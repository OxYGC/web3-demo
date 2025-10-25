package com.web3.entity.dto.eth;

import com.web3.entity.dto.eth.tx.Eip1559DynamicFeeTx;
import com.web3.entity.dto.eth.tx.LegacyFeeTx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 以太坊密钥对与地址结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EthereumSchemaReq {
    private String requestId;
    private Eip1559DynamicFeeTx dynamicFeeTx;
    private LegacyFeeTx classicFeeTx;



}