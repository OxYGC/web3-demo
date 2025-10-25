package com.web3.dao;

public class PrivateKeyWrapper {
    private final String privateKey;  // 实际场景中不应该明文存储

    public PrivateKeyWrapper(String privateKey) {
        this.privateKey = privateKey;
    }

    // 不提供 getPrivateKey()，避免外部直接拿到
    // 只提供签名方法
    public byte[] sign(byte[] message) {
        // 这里调用 secp256k1 或者其它算法签名
        // 返回签名结果，而不是私钥
//        return CryptoUtils.sign(privateKey, message);
        // todo 签名之后进行返回
        return message;
    }
}