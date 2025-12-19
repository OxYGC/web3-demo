package com.web3.demo.ecdsa;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import java.math.BigInteger;

public final class ECDSASign {

    // secp256k1 曲线参数
    private static final X9ECParameters CURVE =
            org.bouncycastle.crypto.ec.CustomNamedCurves.getByName("secp256k1");

    // ECDSA domain parameters（⚠️ signer 需要的是这个）
    private static final ECDomainParameters DOMAIN =
            new ECDomainParameters(
                    CURVE.getCurve(),
                    CURVE.getG(),
                    CURVE.getN(),
                    CURVE.getH()
            );

    private ECDSASign() {
    }

    /**
     * 使用 RFC6979 (deterministic k) 的 ECDSA 签名
     *
     * @param hash       已经 hash 过的消息（32 bytes）
     * @param privateKey secp256k1 私钥
     * @return BigInteger[]{r, s}
     */
    public static BigInteger[] sign(byte[] hash, BigInteger privateKey) {
        if (hash == null || hash.length == 0) {
            throw new IllegalArgumentException("hash is empty");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is null");
        }

        // 使用 RFC6979，避免随机数攻击：RFC6979 的核心思想是：不要相信随机数，直接用数学确定性算法生成 k。RFC6979 deterministic k (no RNG)
        // 直接理解：K依然存在，只是你不用管这个K的值了，RFC6979用自己的方案给你托管随机K这个问题了, 相当于给Ecdsa打个补丁.
        /**
         * 历史上，大量加密货币被盗，不是因为算法弱，而是因为随机数出错。
         * ECDSA 的“致命点”ECDSA 每次签名都需要一个随机数 k：
         * 	•	如果 k：重复不够随机被预测
         * 👉 私钥可以被直接算出来
         * 📌 真实事故：Sony PS3 / Android Bitcoin 钱包 /多个交易所签名模块
         */
        ECDSASigner signer = new ECDSASigner(
                new HMacDSAKCalculator(new SHA256Digest())
        );

        /**
         * RNG = Random Number Generator（随机数生成器）
         * error show:
         * k的值是：new SecureRandom()
         * 这个写法是有风险的，并不是说严格意义的错误，但是ps3的那次事件是把这里new SecureRandom()换成了一个常量
         */
//        ECDSASigner signerError = new ECDSASigner();
//        signerError.init(true, new ParametersWithRandom(new ECPrivateKeyParameters(privateKey, DOMAIN), new SecureRandom())
//        );

        signer.init(true, new ECPrivateKeyParameters(privateKey, DOMAIN));

        BigInteger[] sig = signer.generateSignature(hash);

        /**
         * r 来自 k·G 的 x 坐标(固定)对曲线阶取模的结果
         * s ≡ k⁻¹ (hash + r·d) (mod n)： 由于模运算的天然对称性，所以s会有两个值
         * 就像：+5 ≡ -5 (mod 10) 注意: 这里跟几何Y轴没有关系，很多同学会搞混这里
         */
        // Ethereum / Bitcoin 通用的 low-s 规范
        BigInteger r = sig[0];
        BigInteger s = sig[1];


        /**
         * 如果 (r, s) 是合法签名，那 (r, n - s) 也是合法签名：其中 n 是椭圆曲线的阶（一个固定的大素数）
         * low-s 并不是为了防止别人“算回原始签名”，而是为了让系统里每一笔签名只有一个标准形态，这是分布式系统一致性与安全审计的基础。
         */
        //如果 s 比一半的 n 还大,就把它换成 n - s (s 永远落在 较小的一半区间)
        BigInteger halfN = DOMAIN.getN().shiftRight(1);
        if (s.compareTo(halfN) > 0) {
            s = DOMAIN.getN().subtract(s);
        }

        return new BigInteger[]{r, s};
    }
}