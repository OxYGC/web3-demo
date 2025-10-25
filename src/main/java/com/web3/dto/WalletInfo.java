package com.web3.dto;

public class WalletInfo {

    private String blockchain;
    private String address;
    private String privateKey;
    private String publicKey;
    private Integer index;
    private String derivationPath;

    public WalletInfo() {}

    public WalletInfo(String blockchain, String address, String privateKey, String publicKey, Integer index, String derivationPath) {
        this.blockchain = blockchain;
        this.address = address;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.index = index;
        this.derivationPath = derivationPath;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getDerivationPath() {
        return derivationPath;
    }

    public void setDerivationPath(String derivationPath) {
        this.derivationPath = derivationPath;
    }
}
