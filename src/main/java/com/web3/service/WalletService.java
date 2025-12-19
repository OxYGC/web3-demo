package com.web3.service;

import com.web3.dto.WalletInfo;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * WalletService: 基于 mnemonic/seed 批量派生地址（线程安全）
 * <p>
 * - 支持不同的 derivation base path (accountKey) 例如 BIP44, BIP49, BIP84
 * - 使用 DeterministicHierarchy 来派生 child key
 */
@Service
public class WalletService {
    private NetworkParameters params;
    private DeterministicHierarchy dh;
    private DeterministicKey rootKey; // master xprv
    private String chainId;

    private final SecureRandom secureRandom = new SecureRandom();

    public WalletService() {
        // 默认构造函数
    }

    public WalletService(NetworkParameters params, DeterministicKey rootKey, DeterministicHierarchy dh, String chainId) {
        this.params = params;
        this.rootKey = rootKey;
        this.dh = dh;
        this.chainId = chainId;
    }


    /**
     * 生成助记词
     */
    public String generateMnemonic(int wordCount) {
        try {
            // 计算所需的熵位数
            int entropyBits = wordCount * 11 - (wordCount * 11 / 33);
            byte[] entropy = new byte[entropyBits / 8];
            secureRandom.nextBytes(entropy);

            // 使用Web3j的MnemonicUtils生成助记词
            return MnemonicUtils.generateMnemonic(entropy);
        } catch (Exception e) {
            throw new RuntimeException("生成助记词失败", e);
        }
    }





    /**
     * 生成随机私钥
     */
    public String generatePrivateKey() {
        byte[] privateKeyBytes = new byte[32];
        secureRandom.nextBytes(privateKeyBytes);
        return Numeric.toHexString(privateKeyBytes);
    }

    /**
     * 验证助记词
     */
    public boolean validateMnemonic(String mnemonic) {
        try {
            return MnemonicUtils.validateMnemonic(mnemonic);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据助记词批量生成钱包
     */
    public List<WalletInfo> generateWalletsFromMnemonic(String mnemonic, List<String> blockchains, int count, int startIndex) {
        List<WalletInfo> wallets = new ArrayList<>();

        for (String blockchain : blockchains) {
            for (int i = 0; i < count; i++) {
                int index = startIndex + i;
                WalletInfo wallet = generateWalletFromMnemonic(mnemonic, blockchain, index);
                if (wallet != null) {
                    wallets.add(wallet);
                }
            }
        }

        return wallets;
    }

    /**
     * 根据私钥批量生成钱包
     */
    public List<WalletInfo> generateWalletsFromPrivateKey(String privateKey, List<String> blockchains) {
        List<WalletInfo> wallets = new ArrayList<>();

        for (String blockchain : blockchains) {
            WalletInfo wallet = generateWalletFromPrivateKey(privateKey, blockchain);
            if (wallet != null) {
                wallets.add(wallet);
            }
        }

        return wallets;
    }

    /**
     * 从助记词生成单个钱包
     */
    private WalletInfo generateWalletFromMnemonic(String mnemonic, String blockchain, int index) {
        try {
            switch (blockchain.toUpperCase()) {
                case "BTC":
                    return generateBTCWalletFromMnemonic(mnemonic, index);
                case "ETH":
                    return generateETHWalletFromMnemonic(mnemonic, index);
                case "SOL":
                    return generateSOLWalletFromMnemonic(mnemonic, index);
                default:
                    throw new IllegalArgumentException("不支持的区块链类型: " + blockchain);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从私钥生成单个钱包
     */
    private WalletInfo generateWalletFromPrivateKey(String privateKey, String blockchain) {
        try {
            switch (blockchain.toUpperCase()) {
                case "BTC":
                    return generateBTCWalletFromPrivateKey(privateKey);
                case "ETH":
                    return generateETHWalletFromPrivateKey(privateKey);
                case "SOL":
                    return generateSOLWalletFromPrivateKey(privateKey);
                default:
                    throw new IllegalArgumentException("不支持的区块链类型: " + blockchain);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成BTC钱包（从助记词）
     */
    private WalletInfo generateBTCWalletFromMnemonic(String mnemonic, int index) throws Exception {
        // 使用BIP44路径 m/44'/0'/0'/0/index
        String derivationPath = String.format("m/44'/0'/0'/0/%d", index);

        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);

        // 派生到指定路径 - 使用ChildNumber
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey, new ChildNumber(44, true));
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey, new ChildNumber(0, true));
        DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(coinKey, new ChildNumber(0, true));
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(0, false));
        DeterministicKey addressKey = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(index, false));

        String privateKeyHex = addressKey.getPrivateKeyAsHex();
        String publicKeyHex = addressKey.getPublicKeyAsHex();

        // 生成Legacy地址
        ECKey ecKey = ECKey.fromPrivate(addressKey.getPrivKey());
        Address address = Address.fromKey(MainNetParams.get(), ecKey, Script.ScriptType.P2PKH);

        return new WalletInfo("BTC", address.toString(), privateKeyHex, publicKeyHex, index, derivationPath);
    }

    /**
     * 生成BTC钱包（从私钥）
     */
    private WalletInfo generateBTCWalletFromPrivateKey(String privateKey) throws Exception {
        String cleanPrivateKey = privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(cleanPrivateKey, 16));

        Address address = Address.fromKey(MainNetParams.get(), ecKey, Script.ScriptType.P2PKH);
        String publicKeyHex = ecKey.getPublicKeyAsHex();

        return new WalletInfo("BTC", address.toString(), cleanPrivateKey, publicKeyHex, 0, "");
    }

    /**
     * 生成ETH钱包（从助记词）
     */
    private WalletInfo generateETHWalletFromMnemonic(String mnemonic, int index) throws Exception {
        // 使用BIP44路径 m/44'/60'/0'/0/index
        String derivationPath = String.format("m/44'/60'/0'/0/%d", index);

        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);

        // 正确实现BIP44路径派生 m/44'/60'/0'/0/index
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey, new ChildNumber(44, true));  // m/44'
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey, new ChildNumber(60, true));    // m/44'/60'
        DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(coinKey, new ChildNumber(0, true));     // m/44'/60'/0'
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(0, false));  // m/44'/60'/0'/0
        DeterministicKey addressKey = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(index, false)); // m/44'/60'/0'/0/index

        // 从派生的私钥创建ECKeyPair
        BigInteger privateKeyBigInt = addressKey.getPrivKey();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKeyBigInt);

        String address = Keys.getAddress(ecKeyPair);
        String privateKeyHex = Numeric.toHexString(ecKeyPair.getPrivateKey().toByteArray());
        String publicKeyHex = Numeric.toHexString(ecKeyPair.getPublicKey().toByteArray());

        return new WalletInfo("ETH", "0x" + address, privateKeyHex, publicKeyHex, index, derivationPath);
    }

    /**
     * 生成ETH钱包（从私钥）
     */
    private WalletInfo generateETHWalletFromPrivateKey(String privateKey) throws Exception {
        String cleanPrivateKey = privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(cleanPrivateKey, 16));

        String address = Keys.getAddress(ecKeyPair);
        String publicKeyHex = Numeric.toHexString(ecKeyPair.getPublicKey().toByteArray());

        return new WalletInfo("ETH", "0x" + address, cleanPrivateKey, publicKeyHex, 0, "");
    }

    /**
     * 生成SOL钱包（从助记词）
     */
    private WalletInfo generateSOLWalletFromMnemonic(String mnemonic, int index) throws Exception {
        // 简化实现，实际需要使用ed25519曲线
        String derivationPath = String.format("m/44'/501'/0'/0/%d", index);

        // 这里需要实现Solana的地址生成逻辑
        // 由于复杂性，这里返回一个示例
        return new WalletInfo("SOL", "示例SOL地址" + index, "示例私钥", "示例公钥", index, derivationPath);
    }

    /**
     * 生成SOL钱包（从私钥）
     */
    private WalletInfo generateSOLWalletFromPrivateKey(String privateKey) throws Exception {
        // 简化实现
        return new WalletInfo("SOL", "示例SOL地址", privateKey, "示例公钥", 0, "");
    }













    /**
     * Derive an account node for a given purpose and coinType and account (BIP44/BIP84/BIP49)
     * return DeterministicKey for m / purpose' / coin' / account'
     */
    public DeterministicKey getAccountKey(int purpose, int coinType, int account) {
        // build path m / purpose' / coinType' / account'
        List<ChildNumber> acctPath = Arrays.asList(
                new ChildNumber(purpose, true),
                new ChildNumber(coinType, true),
                new ChildNumber(account, true)
        );
        return dh.get(acctPath, false, true);
    }



    // Factory helpers
    public static WalletService createFromMnemonic(String mnemonic, String passphrase, NetworkParameters params, String chainId) throws UnreadableWalletException {
        long creationTimeSeconds = System.currentTimeMillis() / 1000L;
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, creationTimeSeconds);
        // use seed to create root private key
        DeterministicKey rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        DeterministicHierarchy dh = new DeterministicHierarchy(rootKey);
        return new WalletService(params, rootKey, dh, chainId);
    }
}