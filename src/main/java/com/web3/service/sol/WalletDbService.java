package com.web3.service.sol;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模拟数据库存储服务
 */
@Service
public class WalletDbService {

    /**
     * 数据库 Key 存储对象
     */
    public static class KeyItem {
        private String privateKey;
        private String pubKey;

        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
        public String getPubKey() { return pubKey; }
        public void setPubKey(String pubKey) { this.pubKey = pubKey; }
    }

    /**
     * 存储密钥列表
     */
    public boolean storeKeys(List<KeyItem> keyItems) {
        // TODO: 接入数据库，这里模拟返回 true
        return true;
    }
}