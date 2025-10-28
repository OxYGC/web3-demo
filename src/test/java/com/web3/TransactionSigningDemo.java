package com.web3;

import java.util.*;

/**
 * 比特币交易签名演示程序
 * 展示完整的交易创建、签名、广播流程
 */
public class TransactionSigningDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 比特币交易签名演示程序");
            System.out.println("=" .repeat(50));
            
            // 1. 创建钱包
            String mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
            BdkWalletHelper wallet = new BdkWalletHelper(mnemonic, BdkWalletHelper.TESTNET);
            
            System.out.println("💼 钱包信息:");
            System.out.println("   网络: " + wallet.getNetwork());
            System.out.println("   余额: " + wallet.getBalanceBTC() + " BTC");
            System.out.println("   地址: " + wallet.getNewAddress());
            System.out.println();
            
            // 2. 演示基本交易签名流程
            demonstrateBasicTransaction(wallet);
            
            // 3. 演示PSBT流程
            demonstratePSBTFlow(wallet);
            
            // 4. 演示批量交易
            demonstrateBatchTransaction(wallet);
            
            // 5. 显示交易历史
            displayTransactionHistory(wallet);
            
        } catch (Exception e) {
            System.err.println("❌ 演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示基本交易签名流程
     */
    private static void demonstrateBasicTransaction(BdkWalletHelper wallet) throws Exception {
        System.out.println("📝 演示1: 基本交易签名流程");
        System.out.println("-".repeat(30));
        
        String toAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx";
        long amount = 50000L; // 0.0005 BTC
        float feeRate = 1.5f;
        
        System.out.println("发送参数:");
        System.out.println("  目标地址: " + toAddress);
        System.out.println("  发送金额: " + BdkWalletHelper.satoshiToBTC(amount) + " BTC");
        System.out.println("  手续费率: " + feeRate + " sat/vB");
        System.out.println();
        
        // 发送交易（包含完整的签名流程）
        String txid = wallet.sendBitcoin(toAddress, amount, feeRate);
        
        System.out.println("✅ 交易完成!");
        System.out.println("   交易ID: " + txid);
        System.out.println("   新余额: " + wallet.getBalanceBTC() + " BTC");
        System.out.println();
    }
    
    /**
     * 演示PSBT（部分签名比特币交易）流程
     */
    private static void demonstratePSBTFlow(BdkWalletHelper wallet) throws Exception {
        System.out.println("📝 演示2: PSBT签名流程");
        System.out.println("-".repeat(30));
        
        String toAddress = "tb1q9vza2e8x573nczrlzms0wvx3gsqjx7vavgkx0l";
        long amount = 30000L; // 0.0003 BTC
        float feeRate = 2.0f;
        
        System.out.println("PSBT参数:");
        System.out.println("  目标地址: " + toAddress);
        System.out.println("  发送金额: " + BdkWalletHelper.satoshiToBTC(amount) + " BTC");
        System.out.println("  手续费率: " + feeRate + " sat/vB");
        System.out.println();
        
        // 步骤1: 创建PSBT
        String psbt = wallet.createPSBT(toAddress, amount, feeRate);
        System.out.println("📄 PSBT已创建: " + psbt.substring(0, 50) + "...");
        
        // 步骤2: 签名PSBT
        String signedPsbt = wallet.signPSBT(psbt);
        System.out.println("✍️ PSBT已签名: " + signedPsbt.substring(0, 50) + "...");
        
        // 步骤3: 最终化并广播PSBT
        String txid = wallet.finalizePSBT(signedPsbt);
        System.out.println("✅ PSBT交易完成!");
        System.out.println("   交易ID: " + txid);
        System.out.println();
    }
    
    /**
     * 演示批量交易
     */
    private static void demonstrateBatchTransaction(BdkWalletHelper wallet) throws Exception {
        System.out.println("📝 演示3: 批量交易签名");
        System.out.println("-".repeat(30));
        
        // 准备多个接收者
        List<Map<String, Object>> recipients = new ArrayList<>();
        
        Map<String, Object> recipient1 = new HashMap<>();
        recipient1.put("address", "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sL5k7");
        recipient1.put("amount", 20000L);
        recipients.add(recipient1);
        
        Map<String, Object> recipient2 = new HashMap<>();
        recipient2.put("address", "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx");
        recipient2.put("amount", 15000L);
        recipients.add(recipient2);
        
        Map<String, Object> recipient3 = new HashMap<>();
        recipient3.put("address", "tb1q9vza2e8x573nczrlzms0wvx3gsqjx7vavgkx0l");
        recipient3.put("amount", 10000L);
        recipients.add(recipient3);
        
        System.out.println("批量发送参数:");
        for (int i = 0; i < recipients.size(); i++) {
            Map<String, Object> recipient = recipients.get(i);
            System.out.println("  接收者" + (i+1) + ": " + recipient.get("address"));
            System.out.println("    金额: " + BdkWalletHelper.satoshiToBTC((Long)recipient.get("amount")) + " BTC");
        }
        System.out.println();
        
        // 执行批量交易
        String txid = wallet.sendToMultiple(recipients, 1.8f);
        
        System.out.println("✅ 批量交易完成!");
        System.out.println("   交易ID: " + txid);
        System.out.println("   发送总数: " + recipients.size() + " 个地址");
        System.out.println();
    }
    
    /**
     * 显示交易历史
     */
    private static void displayTransactionHistory(BdkWalletHelper wallet) {
        System.out.println("📊 交易历史记录");
        System.out.println("-".repeat(30));
        
        List<Map<String, Object>> transactions = wallet.getTransactionHistory();
        
        if (transactions.isEmpty()) {
            System.out.println("暂无交易记录");
            return;
        }
        
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> tx = transactions.get(i);
            System.out.println("交易 " + (i + 1) + ":");
            System.out.println("  TXID: " + tx.get("txid"));
            System.out.println("  类型: " + tx.get("type"));
            System.out.println("  状态: " + tx.get("status"));
            
            if ("send".equals(tx.get("type"))) {
                System.out.println("  目标: " + tx.get("toAddress"));
                System.out.println("  金额: " + BdkWalletHelper.satoshiToBTC((Long)tx.get("amount")) + " BTC");
            } else if ("batch_send".equals(tx.get("type"))) {
                System.out.println("  批量发送: " + ((List<?>)tx.get("recipients")).size() + " 个地址");
                System.out.println("  总金额: " + BdkWalletHelper.satoshiToBTC((Long)tx.get("totalAmount")) + " BTC");
            } else if ("receive".equals(tx.get("type"))) {
                System.out.println("  接收金额: " + BdkWalletHelper.satoshiToBTC((Long)tx.get("amount")) + " BTC");
            }
            
            System.out.println("  手续费: " + BdkWalletHelper.satoshiToBTC((Long)tx.get("fee")) + " BTC");
            System.out.println("  时间: " + new Date((Long)tx.get("timestamp")));
            System.out.println();
        }
        
        System.out.println("📈 钱包最终状态:");
        Map<String, Object> walletInfo = wallet.getWalletInfo();
        System.out.println("  余额: " + walletInfo.get("balanceBTC") + " BTC");
        System.out.println("  确认余额: " + BdkWalletHelper.satoshiToBTC((Long)walletInfo.get("confirmedBalance")) + " BTC");
        System.out.println("  未确认余额: " + BdkWalletHelper.satoshiToBTC((Long)walletInfo.get("unconfirmedBalance")) + " BTC");
        System.out.println("  地址数量: " + walletInfo.get("addressCount"));
        System.out.println("  交易数量: " + walletInfo.get("transactionCount"));
    }
}
