package com.web3.service;

import com.web3.BdkWalletHelper;
import org.bitcoindevkit.Network;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BDK钱包服务
 * 提供基于BDK的比特币钱包管理功能
 */
@Service
public class BdkWalletService {
    
    // 钱包缓存，避免重复创建
    private final Map<String, BdkWalletHelper> walletCache = new ConcurrentHashMap<>();
    
    /**
     * 创建或获取钱包实例
     * @param mnemonic 助记词
     * @param network 网络类型
     * @return BdkWalletHelper实例
     */
    public BdkWalletHelper getOrCreateWallet(String mnemonic, Network network) throws Exception {
        String key = mnemonic.hashCode() + "_" + network.toString();
        
        return walletCache.computeIfAbsent(key, k -> {
            try {
                switch (network) {
                    case BITCOIN:
                        return BdkWalletHelper.createMainnet(mnemonic);
                    case TESTNET:
                        return BdkWalletHelper.createTestnet(mnemonic);
                    case REGTEST:
                        return BdkWalletHelper.createRegtest(mnemonic);
                    default:
                        throw new IllegalArgumentException("不支持的网络类型: " + network);
                }
            } catch (Exception e) {
                throw new RuntimeException("创建钱包失败", e);
            }
        });
    }
    
    /**
     * 同步钱包
     * @param mnemonic 助记词
     * @param network 网络类型
     */
    public void syncWallet(String mnemonic, Network network) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync();
    }
    
    /**
     * 获取钱包余额
     * @param mnemonic 助记词
     * @param network 网络类型
     * @return 余额信息
     */
    public Map<String, Object> getWalletBalance(String mnemonic, Network network) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync(); // 同步后获取最新余额
        
        return Map.of(
            "total", wallet.getBalance(),
            "confirmed", wallet.getConfirmedBalance(),
            "unconfirmed", wallet.getUnconfirmedBalance(),
            "totalBtc", BdkWalletHelper.satoshiToBtc(wallet.getBalance()),
            "confirmedBtc", BdkWalletHelper.satoshiToBtc(wallet.getConfirmedBalance()),
            "unconfirmedBtc", BdkWalletHelper.satoshiToBtc(wallet.getUnconfirmedBalance())
        );
    }
    
    /**
     * 获取新地址
     * @param mnemonic 助记词
     * @param network 网络类型
     * @return 比特币地址
     */
    public String getNewAddress(String mnemonic, Network network) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        return wallet.getNewAddress();
    }
    
    /**
     * 获取地址列表
     * @param mnemonic 助记词
     * @param network 网络类型
     * @param count 地址数量
     * @return 地址列表
     */
    public List<String> getAddresses(String mnemonic, Network network, int count) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                try {
                    return wallet.getAddressByIndex(i);
                } catch (Exception e) {
                    throw new RuntimeException("获取地址失败", e);
                }
            })
            .toList();
    }
    
    /**
     * 发送比特币
     * @param mnemonic 助记词
     * @param network 网络类型
     * @param toAddress 目标地址
     * @param amount 金额（satoshi）
     * @param feeRate 手续费率（sat/vB）
     * @return 交易ID
     */
    public String sendBitcoin(String mnemonic, Network network, String toAddress, 
                             long amount, Float feeRate) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync(); // 发送前同步
        
        return wallet.sendToAddress(toAddress, amount, feeRate);
    }
    
    /**
     * 获取交易历史
     * @param mnemonic 助记词
     * @param network 网络类型
     * @return 交易列表
     */
    public List<Map<String, Object>> getTransactionHistory(String mnemonic, Network network) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync(); // 同步后获取最新交易
        
        return wallet.listTransactions();
    }
    
    /**
     * 估算交易手续费
     * @param mnemonic 助记词
     * @param network 网络类型
     * @param toAddress 目标地址
     * @param amount 金额（satoshi）
     * @param feeRate 手续费率（sat/vB）
     * @return 预估手续费（satoshi）
     */
    public long estimateTransactionFee(String mnemonic, Network network, String toAddress, 
                                      long amount, float feeRate) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync(); // 同步后估算
        
        return wallet.estimateFee(toAddress, amount, feeRate);
    }
    
    /**
     * 验证地址
     * @param address 比特币地址
     * @param network 网络类型
     * @return 是否有效
     */
    public boolean validateAddress(String address, Network network) {
        try {
            // 创建临时钱包实例进行验证
            String tempMnemonic = BdkWalletHelper.generateMnemonic();
            BdkWalletHelper wallet = getOrCreateWallet(tempMnemonic, network);
            return wallet.isValidAddress(address);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成助记词
     * @param wordCount 单词数量（忽略，固定生成12个单词）
     * @return 助记词
     */
    public String generateMnemonic(int wordCount) throws Exception {
        return BdkWalletHelper.generateMnemonic();
    }
    
    /**
     * 验证助记词
     * @param mnemonic 助记词
     * @return 是否有效
     */
    public boolean validateMnemonic(String mnemonic) {
        return BdkWalletHelper.validateMnemonic(mnemonic);
    }
    
    /**
     * 获取钱包完整信息
     * @param mnemonic 助记词
     * @param network 网络类型
     * @return 钱包信息
     */
    public Map<String, Object> getWalletInfo(String mnemonic, Network network) throws Exception {
        BdkWalletHelper wallet = getOrCreateWallet(mnemonic, network);
        wallet.sync(); // 同步后获取最新信息
        
        Map<String, Object> info = wallet.getWalletInfo();
        
        // 添加额外信息
        info.put("newAddress", wallet.getNewAddress());
        info.put("transactionCount", wallet.listTransactions().size());
        
        return info;
    }
    
    /**
     * 清理钱包缓存
     */
    public void clearWalletCache() {
        walletCache.values().forEach(BdkWalletHelper::close);
        walletCache.clear();
    }
    
    /**
     * 关闭特定钱包
     * @param mnemonic 助记词
     * @param network 网络类型
     */
    public void closeWallet(String mnemonic, Network network) {
        String key = mnemonic.hashCode() + "_" + network.toString();
        BdkWalletHelper wallet = walletCache.remove(key);
        if (wallet != null) {
            wallet.close();
        }
    }
}
