package com.web3;

import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthereumWalletHelper {

    private final Web3j web3j;

    // 构造函数：传入 RPC URL（如 Sepolia）
    public EthereumWalletHelper(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    // 1. 生成新钱包（测试用！）
    public EthWallet createWallet(String password) throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        WalletFile walletFile = Wallet.createStandard(password, keyPair);
        String address = "0x" + walletFile.getAddress();
        String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
        return new EthWallet(address, privateKey);
    }

    // 2. 从私钥导出地址
    public String getAddressFromPrivateKey(String privateKeyHex) {
        BigInteger privateKey = Numeric.toBigInt(privateKeyHex);
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        return "0x" + Keys.getAddress(keyPair);
    }

    // 3. 查询 ETH 余额（单位：ETH）
    public BigDecimal getBalance(String address) throws Exception {
        org.web3j.protocol.core.methods.response.EthGetBalance balanceWei =
                web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(String.valueOf(balanceWei.getBalance()), Convert.Unit.ETHER);
    }

    // 4. 获取 nonce（用于交易）
    public BigInteger getNonce(String address) throws Exception {
        return web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING)
                .send()
                .getTransactionCount();
    }

    // 5. 签名 ETH 转账交易（提现）
    public String signTransaction(
            String fromPrivateKey,
            String toAddress,
            BigDecimal ethAmount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            BigInteger chainId) throws Exception {
        // 1. 获取 nonce
        String fromAddress = getAddressFromPrivateKey(fromPrivateKey);
        BigInteger nonce = getNonce(fromAddress);

        // 2. 转换金额为 Wei
        BigInteger value = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();

        // 3. 构建 RawTransaction
        RawTransaction rawTx = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, toAddress, value, "");

        // 4. 签名
        Credentials credentials = Credentials.create(fromPrivateKey);
        byte[] signedBytes = TransactionEncoder.signMessage(rawTx, credentials);
        return Numeric.toHexString(signedBytes);
    }

    // 6. 广播交易（返回 txHash）
    public String sendRawTransaction(String signedTxHex) throws Exception {
        org.web3j.protocol.core.methods.response.EthSendTransaction response =
                web3j.ethSendRawTransaction(signedTxHex).send();
        if (response.hasError()) {
            throw new RuntimeException("Send tx error: " + response.getError().getMessage());
        }
        return response.getTransactionHash();
    }

    // 内部类：钱包数据
    public static class EthWallet {
        public final String address;
        public final String privateKey;

        public EthWallet(String address, String privateKey) {
            this.address = address;
            this.privateKey = privateKey;
        }

        @Override
        public String toString() {
            return "EthWallet{" +
                    "address='" + address + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    '}';
        }
    }



}