package com.web3.util;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;


/**
 * Crypto 工具类
 * 负责在签名机（TEE）内部生成 Solana 密钥对
 */
public class CryptoUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final EdDSANamedCurveSpec ED25519_SPEC = EdDSANamedCurveTable.getByName("Ed25519");

    /**
     * 生成随机私钥（32 字节，Hex）
     */
    public static String generatePrivateKey() {
        byte[] priv = new byte[32];
        SECURE_RANDOM.nextBytes(priv);
//        return Hex.encodeHexString(priv);
        return "";
    }

    /**
     * 根据私钥生成公钥（Ed25519）
     *
     * @param privateKeyHex 私钥 Hex
     * @return 公钥 Hex
     * @throws Exception
     */
    public static String derivePublicKey(String privateKeyHex) throws Exception {
        byte[] privBytes = Hex.decodeHex(privateKeyHex.toCharArray());

        // 1. 构造 Ed25519 私钥
        EdDSAPrivateKeySpec privSpec = new EdDSAPrivateKeySpec(privBytes, ED25519_SPEC);
        EdDSAPrivateKey privKey = new EdDSAPrivateKey(privSpec);

        // 2. 从私钥推导公钥
        EdDSAPublicKeySpec pubSpec = new EdDSAPublicKeySpec(privKey.getA(), ED25519_SPEC);
        EdDSAPublicKey pubKey = new EdDSAPublicKey(pubSpec);
        return Hex.encodeHexString(pubKey.getAbyte());
    }

    /**
     * 压缩公钥（x-only, 32 bytes）
     */
    public static String compressPublicKey(String publicKeyHex) {
        // Ed25519 本身就是 32 字节，直接返回
        return publicKeyHex;
    }

    /**
     * 将公钥转换成 Solana 地址（Base58）
     */
//    public static String pubKeyToAddress(String publicKeyHex) throws Exception {
//        byte[] pubBytes = Hex.decodeHex(publicKeyHex.toCharArray());
//        return Base58.encode(pubBytes);
//    }
}