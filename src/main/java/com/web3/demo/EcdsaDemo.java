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

        // 2. 消息
        String message = "send 1 ETH to Alice";
        byte[] hash = HashUtil.keccak256(message.getBytes(StandardCharsets.UTF_8));

        // 3. 签名
        BigInteger[] sig = ECDSASign.sign(hash, privKey);
        BigInteger r = sig[0];
        BigInteger s = sig[1];

        // 4. 验证
        boolean ok = ECDSAVerify.verify(hash, r, s, pubKey);

        System.out.println("Signature valid: " + ok);
    }
}