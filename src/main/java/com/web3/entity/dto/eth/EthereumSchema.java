package com.web3.entity.dto.eth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 以太坊密钥对与地址结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EthereumSchema {
    private String privateKey;     // 私钥（hex 格式）
    private String publicKey;      // 公钥（非压缩）
    private String compressedPubKey; // 压缩公钥
    private String address;        // 以太坊地址 (0x...)


    // ================= 交易模板相关 =================
    private String chainId;           // 链ID，例如 "1" = Ethereum Mainnet
    private BigInteger nonce;               // 账户 nonce
    private String fromAddress;       // 交易发送方
    private String toAddress;         // 交易接收方
    private BigInteger gasLimit;            // Gas 限额

    //EIP1559 相关新增
    private String maxFeePerGas;      // 最大手续费
    //EIP1559 相关新增
    private String maxPriorityFeePerGas; // 优先费（tip）



    private String amount;            // 转账金额（单位：wei）
    private String contractAddress;   // 合约地址（可选）
    private BigInteger gasPrice;






    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * 将 JSON 字符串解析为 EthereumSchema 对象
     */
    public static EthereumSchema fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, EthereumSchema.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse EthereumSchema from JSON", e);
        }
    }

    /**
     * 可选：将对象转成 JSON
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert EthereumSchema to JSON", e);
        }
    }



    /**
     * 构建默认的 ETH Schema（给调用方参考）
     */
    public static EthereumSchema buildDefaultSchema() {
        EthereumSchema schema = new EthereumSchema();
        schema.setChainId("1"); // Ethereum Mainnet
        schema.setNonce(BigInteger.ZERO);
        schema.setFromAddress("0x0000000000000000000000000000000000000000");
        schema.setToAddress("0x0000000000000000000000000000000000000000");
        schema.setGasLimit(BigInteger.valueOf(21000));
        schema.setMaxFeePerGas("0");
        schema.setMaxPriorityFeePerGas("0");
        schema.setAmount("0");
        schema.setContractAddress(""); // 默认空
        return schema;
    }



}