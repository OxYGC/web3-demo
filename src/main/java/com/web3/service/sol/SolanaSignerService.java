package com.web3.service.sol;

/**
 * 签名机 Service 接口
 * 负责生成密钥对和返回公钥
 */
public interface SolanaSignerService {

    /**
     * 生成单个密钥对
     */
    KeyPairResult createKeyPair() throws Exception;

    /**
     * 密钥对封装
     */
    class KeyPairResult {
        private String privateKey;
        private String publicKey;
        private String compressedPubKey;

        public KeyPairResult() {}

        public KeyPairResult(String privateKey, String publicKey, String compressedPubKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.compressedPubKey = compressedPubKey;
        }

        public String getPrivateKey() { return privateKey; }
        public String getPublicKey() { return publicKey; }
        public String getCompressedPubKey() { return compressedPubKey; }

        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
        public void setCompressedPubKey(String compressedPubKey) { this.compressedPubKey = compressedPubKey; }
    }
}