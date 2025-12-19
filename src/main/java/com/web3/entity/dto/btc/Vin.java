package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vin {

    // 基本 UTXO 引用
    // 前一个交易的 hash (txid)
    private String txid;

    // 前一个交易的输出索引
    private int vout;

    // 解锁脚本 (scriptSig)
    private String scriptSig;

    // 序列号 (通常是 0xFFFFFFFF)
    private long sequence;

    // UTXO 金额 (签名时需要，单位：satoshi)
    private long amount;

    // 前一个输出的锁定脚本 (scriptPubKey)，用于 sighash 计算
    // 辅助字段（非 Bitcoin Core 原生，但签名需要）
    private String scriptPubKey;  // 来自 UTXO 的 scriptPubKey (hex)


    // coinbase 特有
    private String coinbase;      // 如果是挖矿交易，vin 里没有 txid/vout，而是有 coinbase 字段

    // scriptSig (非 SegWit 输入才有)
    private String scriptSigAsm;  // 可读形式 (asm)
    private String scriptSigHex;  // hex 编码

    // 金额 (有些 RPC 会返回)
    private Long value;           // satoshi

    // witness 数据 (SegWit 输入才有)
    private List<String> txinwitness;  // witness 栈里的元素（hex）


    private String address;       // 归属的地址（可选，便于调试）



    public Vin getDefault(){
        Vin vin = new Vin();
        vin.setAmount(0L);
        vin.setSequence(0L);
        //todo 补全所有参数
        return vin;
    }

}
