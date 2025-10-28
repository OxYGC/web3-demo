package com.web3;

import org.bitcoindevkit.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public class BdkExample {
    public static void main(String[] args) {
        // 测试助记词（仅用于开发！）
        String mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

        try {
            System.out.println("🚀 BDK钱包助手测试开始...");

            // 创建测试网钱包
            BdkWalletHelper wallet = BdkWalletHelper.createTestnet(mnemonic);
            System.out.println("✅ 钱包创建成功");

            // 1. 同步钱包
            System.out.println("🔄 同步钱包中...");
            wallet.sync();
            System.out.println("✅ 钱包同步完成");

            // 2. 获取钱包信息
            Map<String, Object> walletInfo = wallet.getWalletInfo();
            System.out.println("📊 钱包信息:");
            System.out.println("   网络: " + walletInfo.get("network"));
            System.out.println("   总余额: " + walletInfo.get("balance") + " sats (" +
                BdkWalletHelper.satoshiToBtc((Long)walletInfo.get("balance")) + " BTC)");
            System.out.println("   确认余额: " + walletInfo.get("confirmedBalance") + " sats");
            System.out.println("   未确认余额: " + walletInfo.get("unconfirmedBalance") + " sats");

            // 3. 获取地址
            String newAddress = wallet.getNewAddress();
            String lastUnusedAddress = wallet.getLastUnusedAddress();
            System.out.println("📬 地址信息:");
            System.out.println("   新地址: " + newAddress);
            System.out.println("   最后未使用地址: " + lastUnusedAddress);

            // 4. 获取多个地址
            System.out.println("📋 前5个地址:");
            for (int i = 0; i < 5; i++) {
                String addr = wallet.getAddressByIndex(i);
                System.out.println("   地址[" + i + "]: " + addr);
            }

            // 5. 列出交易历史
            List<Map<String, Object>> transactions = wallet.listTransactions();
            System.out.println("📜 交易历史 (" + transactions.size() + " 笔):");
            for (Map<String, Object> tx : transactions) {
                System.out.println("   TXID: " + tx.get("txid"));
                System.out.println("   接收: " + tx.get("received") + " sats");
                System.out.println("   发送: " + tx.get("sent") + " sats");
                System.out.println("   手续费: " + tx.get("fee") + " sats");
                System.out.println("   确认时间: " + tx.get("confirmationTime"));
                System.out.println("   ---");
            }

            // 6. 地址验证测试
            System.out.println("🔍 地址验证测试:");
            String[] testAddresses = {
                "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx",  // 有效的测试网地址
                "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",          // 有效的主网地址（但网络不匹配）
                "invalid_address"                               // 无效地址
            };

            for (String addr : testAddresses) {
                boolean isValid = wallet.isValidAddress(addr);
                System.out.println("   " + addr + " -> " + (isValid ? "✅ 有效" : "❌ 无效"));
            }

            // 7. 手续费估算测试
            if (wallet.getBalance() > 0) {
                try {
                    String testAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx";
                    long testAmount = 10000; // 0.0001 BTC
                    float feeRate = 1.0f;    // 1 sat/vB

                    long estimatedFee = wallet.estimateFee(testAddress, testAmount, feeRate);
                    System.out.println("💸 手续费估算:");
                    System.out.println("   发送金额: " + testAmount + " sats");
                    System.out.println("   手续费率: " + feeRate + " sat/vB");
                    System.out.println("   预估手续费: " + estimatedFee + " sats");
                } catch (Exception e) {
                    System.out.println("💸 手续费估算失败: " + e.getMessage());
                }
            }

            // 8. 提现测试（注释掉，避免实际发送）
            /*
            if (wallet.getBalance() > 50000) { // 至少0.0005 BTC
                String toAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx";
                long amount = 10000; // 0.0001 BTC

                System.out.println("💸 发送交易...");
                String txid = wallet.withdraw(toAddress, amount);
                System.out.println("✅ 交易发送成功!");
                System.out.println("   TXID: " + txid);
                System.out.println("   金额: " + amount + " sats");
                System.out.println("   目标地址: " + toAddress);
            }
            */

            // 9. 工具方法测试
            System.out.println("🔧 工具方法测试:");
            System.out.println("   1 BTC = " + BdkWalletHelper.btcToSatoshi(BigDecimal.valueOf(1.0)) + " sats");
            System.out.println("   100000000 sats = " + BdkWalletHelper.satoshiToBtc(100000000) + " BTC");

            // 10. 助记词生成和验证测试
            System.out.println("🎲 助记词测试:");
            String newMnemonic = BdkWalletHelper.generateMnemonic();
            System.out.println("   新助记词: " + newMnemonic);
            System.out.println("   验证结果: " + (BdkWalletHelper.validateMnemonic(newMnemonic) ? "✅ 有效" : "❌ 无效"));

            // 关闭钱包
            wallet.close();
            System.out.println("🏁 测试完成!");

        } catch (Exception e) {
            System.err.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}