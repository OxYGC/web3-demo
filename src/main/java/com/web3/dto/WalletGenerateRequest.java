package com.web3.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public class WalletGenerateRequest {

    private String mnemonic; // 助记词

    private String privateKey; // 私钥

    @Min(value = 1, message = "生成数量最少为1")
    @Max(value = 100, message = "生成数量最多为100")
    private Integer count = 1; // 生成数量

    private List<String> blockchains; // 支持的区块链类型 ["BTC", "ETH", "SOL"]

    private Integer startIndex = 0; // 起始索引，用于HD钱包派生路径

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<String> getBlockchains() {
        return blockchains;
    }

    public void setBlockchains(List<String> blockchains) {
        this.blockchains = blockchains;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }
}
