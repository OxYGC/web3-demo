package com.web3.entity;

/**
 * AddressInfo - 数据结构（要存入地址池的字段）
 */
public class AddressInfo {
    public final String chainId;         // e.g. "BTC_MAINNET"
    public final String address;         // 生成的地址
    public final String pubKeyHex;       // 公钥（压缩） hex
    public final String path;            // derivation path, e.g. m/84'/0'/0'/0/123
    public final int index;

    public AddressInfo(String chainId, String address, String pubKeyHex, String path, int index) {
        this.chainId = chainId;
        this.address = address;
        this.pubKeyHex = pubKeyHex;
        this.path = path;
        this.index = index;
    }

    @Override
    public String toString() {
        return "AddressInfo{" +
                "chainId='" + chainId + '\'' +
                ", address='" + address + '\'' +
                ", pubKeyHex='" + pubKeyHex + '\'' +
                ", path='" + path + '\'' +
                ", index=" + index +
                '}';
    }
}
