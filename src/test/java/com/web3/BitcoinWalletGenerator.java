package com.web3;

import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;

public class BitcoinWalletGenerator {
    public static void main(String[] args) {
        // 设置为测试网
        NetworkParameters params = TestNet3Params.get();

        // 随机生成密钥对
        ECKey key = new ECKey();

        // ✅ 正确方式：生成 Legacy 地址（P2PKH）（测试网地址以 m/n 开头）
        Address address = LegacyAddress.fromKey(params, key);

        // 获取私钥（WIF 格式）
        String privateKeyWIF = key.getPrivateKeyAsWiF(params);

        System.out.println("私钥 (WIF): " + privateKeyWIF);
        System.out.println("地址: " + address.toString());
        System.out.println("网络: " + params.getPaymentProtocolId());
    }
}