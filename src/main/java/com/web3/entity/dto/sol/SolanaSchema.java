package com.web3.entity.dto.sol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

/**
 * Solana 交易模板结构
 * 适用于签名机返回交易模板给钱包服务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolanaSchema {

    private String fromAddress;       // 发起者地址 (Base58)
    private String toAddress;         // 接收者地址 (Base58)
    private String recentBlockhash;   // 最近区块哈希 (防重放)
    private String value;             // 转账金额 (SOL 单位: lamports；代币: token 数量，字符串表示)
    private String contractAddress;   // SPL Token 的 Mint 地址 (SOL 转账时为空或 "0x00")
    private int decimals;             // Token 小数位 (SPL Token 必填，SOL 转账默认为 9)
    private boolean tokenCreate;      // 是否自动创建关联 Token Account (ATA)，仅在代币场景有用
    private String cluster;           // 网络: mainnet / devnet / testnet (可选)

    /**
     * 构建默认的 Solana Schema（给调用方参考）
     */
    public static SolanaSchema buildDefaultSchema() {
        SolanaSchema schema = new SolanaSchema();
        schema.setFromAddress("11111111111111111111111111111111"); // system account
        schema.setToAddress("11111111111111111111111111111111");   // system account
        schema.setRecentBlockhash("ABCDEFG1234567890ABCDEFG1234567890ABCDEFG1234567890"); // 示例哈希
        schema.setValue("1000000"); // 默认转账 0.001 SOL (1 lamport = 10^-9 SOL)
        schema.setContractAddress(""); // 空表示 SOL 转账
        schema.setDecimals(9);         // SOL 默认小数 9
        schema.setTokenCreate(false);  // 默认不创建 ATA
        schema.setCluster("devnet");   // 默认 devnet
        return schema;
    }
}