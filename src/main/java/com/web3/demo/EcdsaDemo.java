package com.web3.demo;

import com.web3.util.ecdsa.ECDSASign;
import com.web3.util.ecdsa.ECDSAVerify;
import com.web3.util.ecdsa.HashUtil;
import com.web3.util.ecdsa.KeyUtil;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class EcdsaDemo {

    public static void main(String[] args) {

        // 1. 生成密钥(生成合法的 secp256k1 私钥)
        BigInteger privKey = KeyUtil.generatePrivateKey();
        //私钥通过椭圆曲线算法生成椭圆曲线点
        ECPoint pubKey = KeyUtil.publicKeyFromPrivate(privKey);
        //打印
        printKey(privKey,pubKey);

        // 2. 消息
        String message = "send 1 ETH to Alice";
        byte[] hash = HashUtil.keccak256(message.getBytes(StandardCharsets.UTF_8));

        // 3. 签名
        BigInteger[] sig = ECDSASign.sign(hash, privKey);
        BigInteger r = sig[0];
        BigInteger s = sig[1];

        final String rHex = r.toString(16);
        final String sHex = s.toString(16);
        System.out.printf("r: %s s: %s%n", rHex, sHex);


        // 4. 验证
        boolean ok = ECDSAVerify.verify(hash, r, s, pubKey);

        System.out.println("Signature valid: " + ok);
    }

    /**
     * 同一个公钥，可以有这些表示方式：
     *      - keccak256(pubkey)[12:] 未压缩：65 bytes ，04 + X + Y
     *      - 压缩：33 bytes，02/03 + X
     *      - 去 04：64 bytes ，X + Y
     *      - Ethereum 地址：20 bytes，keccak256(pubkey)[12:]
     * 对公钥做一次 Keccak256 哈希，然后取最后 20 个字节，作为以太坊地址。
     * 同一个公钥，如果表示不统一，会直接出安全事故。
     *
     * @param privKey
     * @param pubKey
     */
    public static void printKey(BigInteger privKey,ECPoint pubKey){
        // 1. 私钥 hex（32字节）
        String privKeyHex = privKey.toString(16);
        if (privKeyHex.length() < 64) {
            privKeyHex = "0".repeat(64 - privKeyHex.length()) + privKeyHex;
        }
        // 2. 未压缩公钥（65字节，04开头）
        byte[] pubKeyUncompressed = pubKey.getEncoded(false);
        String pubKeyHex = bytesToHex(pubKeyUncompressed);

        System.out.println("Private Key (hex): 0x" + privKeyHex);
        System.out.println("Public Key  (uncompressed): 0x" + pubKeyHex);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}