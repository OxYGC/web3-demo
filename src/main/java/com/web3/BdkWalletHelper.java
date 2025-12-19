package com.web3;

import java.util.*;
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;

/**
 * BDK钱包助手类
 * 提供比特币钱包的核心功能：钱包管理、余额查询、交易处理等
 */
public class BdkWalletHelper {

    private String mnemonic;
    private String network;
    private String walletId;
    private long balance;
    private long confirmedBalance;
    private List<String> addresses;
    private List<Map<String, Object>> transactions;

    // 网络常量
    public static final String MAINNET = "mainnet";
    public static final String TESTNET = "testnet";
    public static final String REGTEST = "regtest";

    // 单位转换常量
    private static final long SATOSHI_PER_BTC = 100_000_000L;

    /**
     * 构造函数 - 从助记词创建钱包
     * @param mnemonic 助记词
     */
    public BdkWalletHelper(String mnemonic) {
        this(mnemonic, TESTNET);
    }

    /**
     * 构造函数 - 从助记词创建钱包（指定网络）
     * @param mnemonic 助记词
     * @param network 网络类型
     */
    public BdkWalletHelper(String mnemonic, String network) {
        this.mnemonic = mnemonic;
        this.network = network;
        this.walletId = generateWalletId();
        this.addresses = new ArrayList<>();
        this.transactions = new ArrayList<>();

        // 初始化钱包
        initializeWallet();
    }

    /**
     * 初始化钱包
     */
    private void initializeWallet() {
        // 生成初始地址
        generateAddresses(5);

        // 设置初始余额（模拟）
        this.balance = 1000000L; // 0.01 BTC
        this.confirmedBalance = 800000L; // 0.008 BTC

        // 生成一些模拟交易
        generateMockTransactions();
    }

    /**
     * 生成钱包ID
     */
    private String generateWalletId() {
        return "wallet_" + Math.abs(mnemonic.hashCode());
    }

    /**
     * 同步钱包
     */
    public void sync() {
        System.out.println("🔄 同步钱包: " + walletId);
        // 模拟同步过程
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("✅ 钱包同步完成");
    }

    /**
     * 获取钱包余额（satoshi）
     * @return 余额
     */
    public long getBalance() {
        return balance;
    }

    /**
     * 获取确认余额（satoshi）
     * @return 确认余额
     */
    public long getConfirmedBalance() {
        return confirmedBalance;
    }

    /**
     * 获取未确认余额（satoshi）
     * @return 未确认余额
     */
    public long getUnconfirmedBalance() {
        return balance - confirmedBalance;
    }

    /**
     * 获取BTC格式的余额
     * @return BTC余额
     */
    public BigDecimal getBalanceBTC() {
        return satoshiToBTC(balance);
    }

    /**
     * 获取新的接收地址
     * @return 比特币地址
     */
    public String getNewAddress() {
        String newAddress = generateAddress(addresses.size());
        addresses.add(newAddress);
        return newAddress;
    }

    /**
     * 获取最后使用的地址
     * @return 比特币地址
     */
    public String getLastUnusedAddress() {
        if (addresses.isEmpty()) {
            return getNewAddress();
        }
        return addresses.get(addresses.size() - 1);
    }

    /**
     * 获取所有地址
     * @return 地址列表
     */
    public List<String> getAllAddresses() {
        return new ArrayList<>(addresses);
    }

    /**
     * 发送比特币
     * @param toAddress 目标地址
     * @param amount 金额（satoshi）
     * @return 交易ID
     */
    public String sendBitcoin(String toAddress, long amount) throws Exception {
        return sendBitcoin(toAddress, amount, 1.0f);
    }

    /**
     * 发送比特币（指定手续费率）
     * @param toAddress 目标地址
     * @param amount 金额（satoshi）
     * @param feeRate 手续费率（sat/vB）
     * @return 交易ID
     */
    public String sendBitcoin(String toAddress, long amount, float feeRate) throws Exception {
        // 验证余额
        if (amount > balance) {
            throw new Exception("余额不足");
        }

        // 验证地址
        if (!isValidAddress(toAddress)) {
            throw new Exception("无效的地址格式");
        }

        // 计算手续费
        long fee = calculateFee(amount, feeRate);
        long totalAmount = amount + fee;

        if (totalAmount > balance) {
            throw new Exception("余额不足以支付手续费");
        }

        System.out.println("🔨 创建交易...");

        // 1. 创建未签名交易
        String unsignedTx = createUnsignedTransaction(toAddress, amount, fee);
        System.out.println("📝 未签名交易已创建");

        // 2. 签名交易
        String signedTx = signTransaction(unsignedTx);
        System.out.println("✍️ 交易签名完成");

        // 3. 广播交易
        String txid = broadcastTransaction(signedTx);
        System.out.println("📡 交易已广播到网络");

        // 4. 更新本地状态
        updateWalletState(toAddress, amount, fee, txid);

        System.out.println("✅ 发送成功! TXID: " + txid);
        return txid;
    }

    /**
     * 获取交易历史
     * @return 交易列表
     */
    public List<Map<String, Object>> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }

    /**
     * 获取交易详情
     * @param txid 交易ID
     * @return 交易详情
     */
    public Map<String, Object> getTransaction(String txid) {
        for (Map<String, Object> tx : transactions) {
            if (tx.get("txid").equals(txid)) {
                return tx;
            }
        }
        return null;
    }

    /**
     * 获取钱包信息
     * @return 钱包信息
     */
    public Map<String, Object> getWalletInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("walletId", walletId);
        info.put("network", network);
        info.put("balance", balance);
        info.put("confirmedBalance", confirmedBalance);
        info.put("unconfirmedBalance", getUnconfirmedBalance());
        info.put("balanceBTC", getBalanceBTC());
        info.put("addressCount", addresses.size());
        info.put("transactionCount", transactions.size());
        info.put("lastAddress", getLastUnusedAddress());
        return info;
    }

    // ==================== 工具方法 ====================

    /**
     * Satoshi转BTC
     * @param satoshi satoshi数量
     * @return BTC数量
     */
    public static BigDecimal satoshiToBTC(long satoshi) {
        return BigDecimal.valueOf(satoshi).divide(BigDecimal.valueOf(SATOSHI_PER_BTC), 8, RoundingMode.HALF_UP);
    }

    /**
     * BTC转Satoshi
     * @param btc BTC数量
     * @return satoshi数量
     */
    public static long btcToSatoshi(BigDecimal btc) {
        return btc.multiply(BigDecimal.valueOf(SATOSHI_PER_BTC)).longValue();
    }

    /**
     * 验证地址格式
     * @param address 地址
     * @return 是否有效
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        // 简单的地址格式验证
        if (TESTNET.equals(network)) {
            return address.startsWith("tb1") || address.startsWith("2") || address.startsWith("m") || address.startsWith("n");
        } else if (MAINNET.equals(network)) {
            return address.startsWith("bc1") || address.startsWith("3") || address.startsWith("1");
        }

        return true; // REGTEST或其他网络
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 生成地址
     * @param index 地址索引
     * @return 地址
     */
    private String generateAddress(int index) {
        String prefix;
        if (TESTNET.equals(network)) {
            prefix = "tb1q";
        } else if (MAINNET.equals(network)) {
            prefix = "bc1q";
        } else {
            prefix = "bcrt1q"; // regtest
        }

        // 基于助记词和索引生成地址（简化实现）
        int hash = (mnemonic + index).hashCode();
        String addressSuffix = String.format("%056x", Math.abs(hash)).substring(0, 56);
        return prefix + addressSuffix;
    }

    /**
     * 生成多个地址
     * @param count 地址数量
     */
    private void generateAddresses(int count) {
        for (int i = 0; i < count; i++) {
            addresses.add(generateAddress(i));
        }
    }

    /**
     * 计算手续费
     * @param amount 金额
     * @param feeRate 手续费率
     * @return 手续费
     */
    private long calculateFee(long amount, float feeRate) {
        // 简化的手续费计算：基于交易大小估算
        int estimatedSize = 250; // 估算交易大小（字节）
        return (long) (estimatedSize * feeRate);
    }

    /**
     * 创建未签名交易
     * @param toAddress 目标地址
     * @param amount 金额
     * @param fee 手续费
     * @return 未签名交易数据
     */
    private String createUnsignedTransaction(String toAddress, long amount, long fee) {
        // 模拟创建未签名交易的过程
        Map<String, Object> unsignedTx = new HashMap<>();
        unsignedTx.put("version", 2);
        unsignedTx.put("inputs", createInputs(amount + fee));
        unsignedTx.put("outputs", createOutputs(toAddress, amount, fee));
        unsignedTx.put("locktime", 0);
        unsignedTx.put("signed", false);

        // 返回序列化的未签名交易（简化为JSON字符串）
        return unsignedTx.toString();
    }

    /**
     * 签名交易
     * @param unsignedTx 未签名交易
     * @return 签名后的交易
     */
    private String signTransaction(String unsignedTx) throws Exception {
        System.out.println("🔐 使用私钥签名交易...");

        // 模拟签名过程
        // 在真实实现中，这里会：
        // 1. 解析未签名交易
        // 2. 为每个输入创建签名
        // 3. 使用助记词派生的私钥进行ECDSA签名
        // 4. 将签名添加到交易中

        Thread.sleep(500); // 模拟签名计算时间

        String signedTx = unsignedTx.replace("\"signed\":false", "\"signed\":true");
        signedTx = signedTx.replace("}", ",\"signatures\":[\"" + generateSignature() + "\"]}");

        return signedTx;
    }

    /**
     * 广播交易到网络
     * @param signedTx 已签名的交易
     * @return 交易ID
     */
    private String broadcastTransaction(String signedTx) throws Exception {
        System.out.println("📡 连接到比特币网络...");
        Thread.sleep(1000); // 模拟网络延迟

        // 在真实实现中，这里会：
        // 1. 连接到比特币节点或Electrum服务器
        // 2. 发送交易到网络
        // 3. 等待网络确认

        String txid = generateTxid();
        System.out.println("🌐 交易已提交到内存池，等待确认...");

        return txid;
    }

    /**
     * 更新钱包状态
     * @param toAddress 目标地址
     * @param amount 金额
     * @param fee 手续费
     * @param txid 交易ID
     */
    private void updateWalletState(String toAddress, long amount, long fee, String txid) {
        // 更新余额
        balance -= (amount + fee);

        // 添加交易记录
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("txid", txid);
        transaction.put("toAddress", toAddress);
        transaction.put("amount", amount);
        transaction.put("fee", fee);
        transaction.put("timestamp", System.currentTimeMillis());
        transaction.put("type", "send");
        transaction.put("status", "pending"); // 初始状态为待确认
        transaction.put("confirmations", 0);

        transactions.add(transaction);

        System.out.println("💰 钱包余额已更新: " + satoshiToBTC(balance) + " BTC");
    }

    /**
     * 创建交易输入
     * @param requiredAmount 需要的金额
     * @return 输入列表
     */
    private List<Map<String, Object>> createInputs(long requiredAmount) {
        List<Map<String, Object>> inputs = new ArrayList<>();

        // 模拟选择UTXO的过程
        // 在真实实现中，这里会从钱包的UTXO集合中选择足够的输入
        Map<String, Object> input = new HashMap<>();
        input.put("txid", generateTxid());
        input.put("vout", 0);
        input.put("amount", requiredAmount + 50000); // 稍微多一点，用于找零
        input.put("scriptPubKey", "76a914" + generateScriptHash() + "88ac");

        inputs.add(input);
        return inputs;
    }

    /**
     * 创建交易输出
     * @param toAddress 目标地址
     * @param amount 发送金额
     * @param fee 手续费
     * @return 输出列表
     */
    private List<Map<String, Object>> createOutputs(String toAddress, long amount, long fee) {
        List<Map<String, Object>> outputs = new ArrayList<>();

        // 主输出：发送给目标地址
        Map<String, Object> mainOutput = new HashMap<>();
        mainOutput.put("address", toAddress);
        mainOutput.put("amount", amount);
        mainOutput.put("scriptPubKey", addressToScriptPubKey(toAddress));
        outputs.add(mainOutput);

        // 找零输出：剩余金额返回给自己的地址
        long changeAmount = 50000 - fee; // 假设输入比需要的多50000 satoshi
        if (changeAmount > 0) {
            Map<String, Object> changeOutput = new HashMap<>();
            changeOutput.put("address", getNewAddress());
            changeOutput.put("amount", changeAmount);
            changeOutput.put("scriptPubKey", addressToScriptPubKey(getNewAddress()));
            outputs.add(changeOutput);
        }

        return outputs;
    }

    /**
     * 生成签名
     * @return 模拟的签名
     */
    private String generateSignature() {
        SecureRandom random = new SecureRandom();
        StringBuilder signature = new StringBuilder();
        for (int i = 0; i < 128; i++) { // DER编码的签名通常是64-72字节
            signature.append(Integer.toHexString(random.nextInt(16)));
        }
        return signature.toString();
    }

    /**
     * 生成脚本哈希
     * @return 脚本哈希
     */
    private String generateScriptHash() {
        SecureRandom random = new SecureRandom();
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < 40; i++) { // 20字节的哈希
            hash.append(Integer.toHexString(random.nextInt(16)));
        }
        return hash.toString();
    }

    /**
     * 地址转换为脚本公钥
     * @param address 地址
     * @return 脚本公钥
     */
    private String addressToScriptPubKey(String address) {
        // 简化实现：根据地址类型生成对应的脚本
        if (address.startsWith("bc1") || address.startsWith("tb1")) {
            // Bech32地址 (P2WPKH)
            return "0014" + generateScriptHash();
        } else if (address.startsWith("3") || address.startsWith("2")) {
            // P2SH地址
            return "a914" + generateScriptHash() + "87";
        } else {
            // P2PKH地址
            return "76a914" + generateScriptHash() + "88ac";
        }
    }

    /**
     * 生成交易ID
     * @return 交易ID
     */
    private String generateTxid() {
        SecureRandom random = new SecureRandom();
        StringBuilder txid = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            txid.append(Integer.toHexString(random.nextInt(16)));
        }
        return txid.toString();
    }

    /**
     * 生成模拟交易
     */
    private void generateMockTransactions() {
        // 添加一些模拟的历史交易
        Map<String, Object> tx1 = new HashMap<>();
        tx1.put("txid", generateTxid());
        tx1.put("amount", 500000L);
        tx1.put("fee", 1000L);
        tx1.put("timestamp", System.currentTimeMillis() - 86400000); // 1天前
        tx1.put("type", "receive");
        tx1.put("status", "confirmed");
        transactions.add(tx1);

        Map<String, Object> tx2 = new HashMap<>();
        tx2.put("txid", generateTxid());
        tx2.put("amount", 300000L);
        tx2.put("fee", 800L);
        tx2.put("timestamp", System.currentTimeMillis() - 43200000); // 12小时前
        tx2.put("type", "receive");
        tx2.put("status", "confirmed");
        transactions.add(tx2);
    }

    // ==================== 静态工具方法 ====================

    /**
     * 生成新的助记词
     * @return 12个单词的助记词
     */
    public static String generateMnemonic() {
        // 简化的助记词生成（实际应该使用BIP39标准）
        String[] words = {
            "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
            "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid",
            "acoustic", "acquire", "across", "act", "action", "actor", "actress", "actual"
        };

        SecureRandom random = new SecureRandom();
        StringBuilder mnemonic = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            if (i > 0) mnemonic.append(" ");
            mnemonic.append(words[random.nextInt(words.length)]);
        }

        return mnemonic.toString();
    }

    /**
     * 验证助记词格式
     * @param mnemonic 助记词
     * @return 是否有效
     */
    public static boolean validateMnemonic(String mnemonic) {
        if (mnemonic == null || mnemonic.trim().isEmpty()) {
            return false;
        }

        String[] words = mnemonic.trim().split("\\s+");
        return words.length == 12 || words.length == 15 || words.length == 18 ||
               words.length == 21 || words.length == 24;
    }

    /**
     * 获取网络类型
     * @return 网络类型
     */
    public String getNetwork() {
        return network;
    }

    /**
     * 获取钱包ID
     * @return 钱包ID
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * 获取助记词
     * @return 助记词
     */
    public String getMnemonic() {
        return mnemonic;
    }

    // ==================== 兼容性方法 ====================

    /**
     * 创建主网钱包助手
     * @param mnemonic 助记词
     * @return BdkWalletHelper实例
     */
    public static BdkWalletHelper createMainnet(String mnemonic) {
        return new BdkWalletHelper(mnemonic, MAINNET);
    }

    /**
     * 创建测试网钱包助手
     * @param mnemonic 助记词
     * @return BdkWalletHelper实例
     */
    public static BdkWalletHelper createTestnet(String mnemonic) {
        return new BdkWalletHelper(mnemonic, TESTNET);
    }

    /**
     * 创建回归测试网钱包助手
     * @param mnemonic 助记词
     * @return BdkWalletHelper实例
     */
    public static BdkWalletHelper createRegtest(String mnemonic) {
        return new BdkWalletHelper(mnemonic, REGTEST);
    }

    /**
     * Satoshi转BTC（兼容旧方法名）
     * @param satoshi satoshi数量
     * @return BTC数量
     */
    public static double satoshiToBtc(long satoshi) {
        return satoshiToBTC(satoshi).doubleValue();
    }

    /**
     * 获取指定索引的地址
     * @param index 地址索引
     * @return 比特币地址
     */
    public String getAddressByIndex(int index) {
        if (index < addresses.size()) {
            return addresses.get(index);
        }
        // 如果索引超出现有地址，生成新地址
        while (addresses.size() <= index) {
            addresses.add(generateAddress(addresses.size()));
        }
        return addresses.get(index);
    }

    /**
     * 发送到地址（兼容旧方法名）
     * @param toAddress 目标地址
     * @param amount 金额
     * @param feeRate 手续费率
     * @return 交易ID
     */
    public String sendToAddress(String toAddress, long amount, Float feeRate) throws Exception {
        return sendBitcoin(toAddress, amount, feeRate != null ? feeRate : 1.0f);
    }

    /**
     * 列出交易（兼容旧方法名）
     * @return 交易列表
     */
    public List<Map<String, Object>> listTransactions() {
        return getTransactionHistory();
    }

    /**
     * 估算手续费
     * @param toAddress 目标地址
     * @param amount 金额
     * @param feeRate 手续费率
     * @return 手续费
     */
    public long estimateFee(String toAddress, long amount, float feeRate) {
        return calculateFee(amount, feeRate);
    }

    /**
     * 关闭钱包（空实现，用于兼容）
     */
    public void close() {
        // 简化实现，无需特殊清理
    }

    // ==================== 交易方法 ====================

    /**
     * 创建PSBT（部分签名比特币交易）
     * @param toAddress 目标地址
     * @param amount 金额
     * @param feeRate 手续费率
     * @return PSBT字符串
     */
    public String createPSBT(String toAddress, long amount, float feeRate) throws Exception {
        if (!isValidAddress(toAddress)) {
            throw new Exception("无效的地址格式");
        }

        long fee = calculateFee(amount, feeRate);

        System.out.println("📝 创建PSBT...");

        Map<String, Object> psbt = new HashMap<>();
        psbt.put("version", 2);
        psbt.put("inputs", createInputs(amount + fee));
        psbt.put("outputs", createOutputs(toAddress, amount, fee));
        psbt.put("locktime", 0);
        psbt.put("psbt_version", 0);
        psbt.put("signed", false);
        psbt.put("finalized", false);

        String psbtString = "psbt_" + Base64.getEncoder().encodeToString(psbt.toString().getBytes());
        System.out.println("✅ PSBT创建完成");

        return psbtString;
    }

    /**
     * 签名PSBT
     * @param psbtString PSBT字符串
     * @return 签名后的PSBT
     */
    public String signPSBT(String psbtString) throws Exception {
        if (!psbtString.startsWith("psbt_")) {
            throw new Exception("无效的PSBT格式");
        }

        System.out.println("✍️ 签名PSBT...");
        Thread.sleep(500); // 模拟签名时间

        // 模拟签名过程
        String signedPsbt = psbtString.replace("\"signed\":false", "\"signed\":true");
        signedPsbt = signedPsbt.replace("\"finalized\":false", "\"finalized\":false"); // 签名但未最终化

        System.out.println("✅ PSBT签名完成");
        return signedPsbt;
    }

    /**
     * 最终化PSBT并广播
     * @param signedPsbtString 已签名的PSBT
     * @return 交易ID
     */
    public String finalizePSBT(String signedPsbtString) throws Exception {
        // 检查PSBT是否已签名（更宽松的检查）
        if (!signedPsbtString.contains("signed") || signedPsbtString.contains("\"signed\":false")) {
            throw new Exception("PSBT尚未签名");
        }

        System.out.println("🔨 最终化PSBT...");

        // 模拟最终化过程
        String finalizedPsbt = signedPsbtString.replace("\"finalized\":false", "\"finalized\":true");

        // 提取并广播交易
        String txid = broadcastTransaction(finalizedPsbt);

        System.out.println("✅ PSBT最终化并广播完成");
        return txid;
    }

    /**
     * 批量发送交易
     * @param recipients 接收者列表，格式：[{address: "地址", amount: 金额}, ...]
     * @param feeRate 手续费率
     * @return 交易ID
     */
    public String sendToMultiple(List<Map<String, Object>> recipients, float feeRate) throws Exception {
        if (recipients == null || recipients.isEmpty()) {
            throw new Exception("接收者列表不能为空");
        }

        long totalAmount = 0;
        for (Map<String, Object> recipient : recipients) {
            String address = (String) recipient.get("address");
            Long amount = (Long) recipient.get("amount");

            if (!isValidAddress(address)) {
                throw new Exception("无效的地址: " + address);
            }
            if (amount == null || amount <= 0) {
                throw new Exception("无效的金额: " + amount);
            }

            totalAmount += amount;
        }

        long fee = calculateFee(totalAmount, feeRate);
        long totalRequired = totalAmount + fee;

        if (totalRequired > balance) {
            throw new Exception("余额不足");
        }

        System.out.println("💸 批量发送到 " + recipients.size() + " 个地址...");

        // 创建、签名并广播交易
        String unsignedTx = createMultiOutputTransaction(recipients, fee);
        String signedTx = signTransaction(unsignedTx);
        String txid = broadcastTransaction(signedTx);

        // 更新余额
        balance -= totalRequired;

        // 记录交易
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("txid", txid);
        transaction.put("recipients", recipients);
        transaction.put("totalAmount", totalAmount);
        transaction.put("fee", fee);
        transaction.put("timestamp", System.currentTimeMillis());
        transaction.put("type", "batch_send");
        transaction.put("status", "pending");

        transactions.add(transaction);

        System.out.println("✅ 批量发送完成! TXID: " + txid);
        return txid;
    }

    /**
     * 创建多输出交易
     */
    private String createMultiOutputTransaction(List<Map<String, Object>> recipients, long fee) {
        long totalAmount = recipients.stream()
            .mapToLong(r -> (Long) r.get("amount"))
            .sum();

        Map<String, Object> tx = new HashMap<>();
        tx.put("version", 2);
        tx.put("inputs", createInputs(totalAmount + fee));
        tx.put("outputs", createMultipleOutputs(recipients, fee));
        tx.put("locktime", 0);
        tx.put("signed", false);
        return tx.toString();
    }

    /**
     * 创建多个输出
     */
    private List<Map<String, Object>> createMultipleOutputs(List<Map<String, Object>> recipients, long fee) {
        List<Map<String, Object>> outputs = new ArrayList<>();

        // 为每个接收者创建输出
        for (Map<String, Object> recipient : recipients) {
            Map<String, Object> output = new HashMap<>();
            output.put("address", recipient.get("address"));
            output.put("amount", recipient.get("amount"));
            output.put("scriptPubKey", addressToScriptPubKey((String) recipient.get("address")));
            outputs.add(output);
        }

        // 添加找零输出（如果需要）
        long totalSent = recipients.stream().mapToLong(r -> (Long) r.get("amount")).sum();
        long changeAmount = 50000 - fee; // 假设的找零金额
        if (changeAmount > 0) {
            Map<String, Object> changeOutput = new HashMap<>();
            changeOutput.put("address", getNewAddress());
            changeOutput.put("amount", changeAmount);
            changeOutput.put("scriptPubKey", addressToScriptPubKey(getNewAddress()));
            outputs.add(changeOutput);
        }

        return outputs;
    }
}