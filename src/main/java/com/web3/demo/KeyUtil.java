package com.web3.demo;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class KeyUtil {

    // secp256k1 曲线参数（⚠️ 必须显式类型）
    private static final X9ECParameters CURVE =
            CustomNamedCurves.getByName("secp256k1");

    // 曲线阶 N（私钥取值范围）
    private static final BigInteger CURVE_N = CURVE.getN();

    // 统一随机源（避免每次 new）
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyUtil() {
        // 工具类禁止实例化
    }

    /**
     * 生成合法的 secp256k1 私钥
     * 范围: [1, n-1]
     */
    public static BigInteger generatePrivateKey() {
        BigInteger d;
        do {
            //完整的私钥
            d = new BigInteger(CURVE_N.bitLength(), RANDOM);
        } while (d.signum() <= 0 || d.compareTo(CURVE_N) >= 0);
        return d;
    }

    /**

     * 从私钥计算公钥
     * Q = d × G
     •	d：私钥（秘密）
     •	G：系统规定的“起点”（所有人都一样）
     •	Q：算出来的点（公钥）
     * 解释： * 系统从一个公开的起点 G 出发，沿着椭圆曲线走 privateKey 次，得到的终点坐标，就是公钥。
     */
    public static ECPoint publicKeyFromPrivate(BigInteger privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is null");
        }
        //私钥 d 必须满足：1 ≤ d < n （n = 椭圆曲线的阶）
        //这一步是为了防止“不同私钥映射到同一个公钥”，否则会破坏整个密码体系的唯一性和安全假设。
        if (privateKey.signum() <= 0 || privateKey.compareTo(CURVE_N) >= 0) {
            throw new IllegalArgumentException("invalid private key range");
        }
        return CURVE.getG().multiply(privateKey).normalize();
    }
}