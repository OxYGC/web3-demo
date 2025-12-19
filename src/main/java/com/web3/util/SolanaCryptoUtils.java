package com.web3.util;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;


import static net.i2p.crypto.eddsa.Utils.hexToBytes;

/**
 * Solana 密钥与地址工具类
 * 适用于 TEE 签名机服务
 */
public class SolanaCryptoUtils {

    /**
     * 将 Hex 字符串转换为字节数组
     */
    public static byte[] hexToBytes(String hex) throws Exception {
        return Hex.decodeHex(hex.toCharArray());
    }

    /**
     * 从私钥生成公钥
     */
    public static byte[] publicKeyFromPrivateKey(byte[] privateKeyBytes) throws Exception {
        if (privateKeyBytes.length != 64) {
            throw new IllegalArgumentException("Invalid Ed25519 private key length");
        }
        Account account = new Account(privateKeyBytes);
        return account.getPublicKey().toByteArray();
    }

    /**
     * 将公钥字节数组转换为 Base58 地址
     */
    public static String publicKeyToAddress(byte[] publicKeyBytes) {
        return Base58.encode(publicKeyBytes);
    }

    /**
     * 从公钥 Hex 获取 Solana 地址
     */
    public static String publicKeyHexToAddress(String publicKeyHex) throws Exception {
        byte[] pubKeyBytes = hexToBytes(publicKeyHex);
        return publicKeyToAddress(pubKeyBytes);
    }

    /**
     * 随机生成新的 Solana KeyPair
     */
    public static Account generateNewKeypair() {
        return new Account(); // 自动生成随机密钥
    }

    /**
     * 从 byte[] 创建 Solana PrivateKey（Account）
     */
    public static Account privateKeyFromBytes(byte[] privateKeyBytes) throws Exception {
        if (privateKeyBytes.length != 64) {
            throw new IllegalArgumentException("Invalid private key length");
        }
        return new Account(privateKeyBytes);
    }

    /**
     * 从 Base58 字符串生成 Solana PrivateKey（Account）
     */
    public static Account privateKeyFromBase58(String privateKeyBase58) throws Exception {
        return new Account(Base58.decode(privateKeyBase58));
    }

    /**
     * 将 Account 私钥导出为 Base58
     */
    public static String privateKeyToBase58(Account account) {
        return Base58.encode(account.getSecretKey());
    }

    /**
     * 从 Account 获取公钥对象
     */
    public static PublicKey publicKeyFromAccount(Account account) {
        return account.getPublicKey();
    }

    /**
     * 从 Base58 公钥字符串生成 PublicKey 对象
     */
    public static PublicKey publicKeyFromBase58(String pubKeyBase58) {
        return new PublicKey(pubKeyBase58);
    }

    /**
     * 将 PublicKey 对象转换为 Base58 地址
     */
    public static String publicKeyToBase58(PublicKey publicKey) {
        return publicKey.toBase58();
    }

    /**
     * 从 PublicKey 获取 Solana 地址（Base58）
     */
    public static String addressFromPublicKey(PublicKey publicKey) {
        return publicKey.toBase58();
    }

}