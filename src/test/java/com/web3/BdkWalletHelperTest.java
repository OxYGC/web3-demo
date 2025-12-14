package com.web3;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.math.BigDecimal;

/**
 * BdkWalletHelper测试类
 */
public class BdkWalletHelperTest {
    
    private BdkWalletHelper wallet;
    private String testMnemonic;
    
    @BeforeEach
    void setUp() {
        testMnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
        wallet = new BdkWalletHelper(testMnemonic, BdkWalletHelper.TESTNET);
    }
    
    @Test
    <E>
    void testWalletCreation() {
        assertNotNull(wallet);
        assertEquals(BdkWalletHelper.TESTNET, wallet.getNetwork());
        assertEquals(testMnemonic, wallet.getMnemonic());
        assertNotNull(wallet.getWalletId());
        List<String> strings = Lists.newArrayList("1", "2");
        Set<String> sets = Sets.newHashSet("1", "2");
    }
    
    @Test
    void testBalanceOperations() {
        // 测试余额查询
        long balance = wallet.getBalance();
        long confirmedBalance = wallet.getConfirmedBalance();
        long unconfirmedBalance = wallet.getUnconfirmedBalance();
        
        assertTrue(balance > 0);
        assertTrue(confirmedBalance >= 0);
        assertTrue(unconfirmedBalance >= 0);
        assertEquals(balance, confirmedBalance + unconfirmedBalance);
        
        // 测试BTC格式余额
        BigDecimal balanceBTC = wallet.getBalanceBTC();
        assertNotNull(balanceBTC);
        assertTrue(balanceBTC.compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    void testAddressGeneration() {
        // 测试地址生成
        String newAddress = wallet.getNewAddress();
        assertNotNull(newAddress);
        assertTrue(newAddress.startsWith("tb1q")); // 测试网地址前缀
        
        String lastAddress = wallet.getLastUnusedAddress();
        assertNotNull(lastAddress);
        
        // 测试按索引获取地址
        String address0 = wallet.getAddressByIndex(0);
        String address1 = wallet.getAddressByIndex(1);
        assertNotNull(address0);
        assertNotNull(address1);
        assertNotEquals(address0, address1);
        
        // 测试获取所有地址
        List<String> addresses = wallet.getAllAddresses();
        assertNotNull(addresses);
        assertTrue(addresses.size() > 0);
    }
    
    @Test
    void testTransactionOperations() throws Exception {
        String testAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx";
        long amount = 100000L; // 0.001 BTC
        
        // 测试发送比特币
        String txid = wallet.sendBitcoin(testAddress, amount);
        assertNotNull(txid);
        // 交易ID长度应该是64个字符
        assertEquals(64, txid.length());
        
        // 测试交易历史
        List<Map<String, Object>> transactions = wallet.getTransactionHistory();
        assertNotNull(transactions);
        assertTrue(transactions.size() > 0);
        
        // 验证交易记录
        boolean foundTransaction = transactions.stream()
            .anyMatch(tx -> txid.equals(tx.get("txid")));
        assertTrue(foundTransaction);
    }
    
    @Test
    void testAddressValidation() {
        // 测试有效地址
        assertTrue(wallet.isValidAddress("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx"));
        assertTrue(wallet.isValidAddress("2MzQwSSnBHWHqSAqtTVQ6v47XtaisrJa1Vc"));
        
        // 测试无效地址
        assertFalse(wallet.isValidAddress(""));
        assertFalse(wallet.isValidAddress(null));
        assertFalse(wallet.isValidAddress("invalid_address"));
    }
    
    @Test
    void testWalletInfo() {
        Map<String, Object> info = wallet.getWalletInfo();
        assertNotNull(info);
        
        assertTrue(info.containsKey("walletId"));
        assertTrue(info.containsKey("network"));
        assertTrue(info.containsKey("balance"));
        assertTrue(info.containsKey("confirmedBalance"));
        assertTrue(info.containsKey("unconfirmedBalance"));
        assertTrue(info.containsKey("balanceBTC"));
        assertTrue(info.containsKey("addressCount"));
        assertTrue(info.containsKey("transactionCount"));
        assertTrue(info.containsKey("lastAddress"));
        
        assertEquals(BdkWalletHelper.TESTNET, info.get("network"));
    }
    
    @Test
    void testStaticMethods() {
        // 测试单位转换
        long satoshi = 100000000L; // 1 BTC
        BigDecimal btc = BdkWalletHelper.satoshiToBTC(satoshi);
        assertEquals(BigDecimal.ONE, btc);
        
        long convertedSatoshi = BdkWalletHelper.btcToSatoshi(btc);
        assertEquals(satoshi, convertedSatoshi);
        
        // 测试助记词生成和验证
        String mnemonic = BdkWalletHelper.generateMnemonic();
        assertNotNull(mnemonic);
        assertTrue(BdkWalletHelper.validateMnemonic(mnemonic));
        
        // 测试无效助记词
        assertFalse(BdkWalletHelper.validateMnemonic(""));
        assertFalse(BdkWalletHelper.validateMnemonic(null));
        assertFalse(BdkWalletHelper.validateMnemonic("invalid"));
    }
    
    @Test
    void testFactoryMethods() {
        // 测试工厂方法
        BdkWalletHelper mainnetWallet = BdkWalletHelper.createMainnet(testMnemonic);
        assertEquals(BdkWalletHelper.MAINNET, mainnetWallet.getNetwork());
        
        BdkWalletHelper testnetWallet = BdkWalletHelper.createTestnet(testMnemonic);
        assertEquals(BdkWalletHelper.TESTNET, testnetWallet.getNetwork());
        
        BdkWalletHelper regtestWallet = BdkWalletHelper.createRegtest(testMnemonic);
        assertEquals(BdkWalletHelper.REGTEST, regtestWallet.getNetwork());
    }
    
    @Test
    void testSync() {
        // 测试同步功能
        assertDoesNotThrow(() -> wallet.sync());
    }
}
