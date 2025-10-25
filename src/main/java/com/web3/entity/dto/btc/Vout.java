package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vout {
    private String address;
    private int index;
    private long amount;

    private int n;                // 输出序号
    private long value;           // 金额 (satoshi)

    // ScriptPubKey
    private String scriptPubKeyAsm;   // 可读形式 (asm)
    private String scriptPubKeyHex;   // hex 编码
    private String type;              // 类型（"pubkeyhash", "scripthash", "witness_v0_keyhash", "witness_v1_taproot"...）
    private List<String> addresses;   // 解析出来的地址列表


    public Vout(String address, int index, long amount) {
        this.address = address;
        this.index = index;
        this.amount = amount;
    }

    // getters & setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
