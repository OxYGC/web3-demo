package com.web3.demo.ecdsa;

import com.web3.util.ecdsa.ECDSAVerify;
import com.web3.util.ecdsa.HashUtil;
import com.web3.demo.KeyUtil;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * R ≡ k × G --> r = x(R) mod n
 * => r 是由签名随机数 k 与生成元 G 相乘得到的椭圆曲线点 R 的 x 坐标，对曲线阶 n 取模后的结果。(n和G都是曲线的标准常量)
 *
 * s ≡ k⁻¹ · (hash + r · d) mod n (取得r之后，r乘以私钥然后和message的Hash加法运算除以随机数k然后对n进行取模，所以输入要素就是：待签名的messageHash,私钥)
 *      其中 r =  x(R) mod n 所以： s ≡ k⁻¹ · (hash + (x(R) mod n) · d) mod n
 * => s是在随机值r的基础上，加入了变量私钥d和签名内容hash (私钥 d（身份绑定）,消息哈希 hash（内容绑定）)
 */
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
     * 打印结果:
     * Private Key (hex): 0xe5e955cce462b8dd039fdbf1c252b93863f63c5af605e49a4a6126281773f65b
     * Public Key  (uncompressed): 0x0404ee8ffcb38a44a587bc190b446e5b03af93c9243f9c179b6a0d7231511ca3361bd996a78bee88154dac696c3de34756b1c71fa28b626a0947050c117f93baf1
     * r: f2d360dbf3293fb3c732ad9bafb1c5d51830a8c45b83960b96243d23b5ef22c6 s: 27f69bcbef35c95a520360d09f2df006888491928f5e2906052031b7a074e076
     * Signature valid: true
     */




    /**
     * 打印生成的公私钥
     *
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