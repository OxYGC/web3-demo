package com.web3.demo.ecdsa.mpc;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.asn1.x9.X9ECParameters;

public class MPCNode {

    /**
     * 返回的是 secp256k1 协议标准的生成元, 不同类型的椭圆曲线他们的G点是不一样的
     * 选定了这个类型,该曲线CURVE的G点就已经确定了,secp256k1的G点值:
     * Gx = 79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798
     * Gy = 483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8
     * n = FFFFFFFF FFFFFFFF FFFFFFFF FFFFFFFE BAAEDCE6 AF48A03B BFD25E8C D0364141
     */
    static final X9ECParameters CURVE =
            CustomNamedCurves.getByName("secp256k1");

    private static final SecureRandom RANDOM = new SecureRandom();

    // 私钥分片（标量）
    private final BigInteger ri;
    // 公钥分片（点）
    private final ECPoint Pi;

    public MPCNode() {
        //私钥分片ri: mod(CURVE.getN()) 保证它在曲线阶范围 [0, n-1]
        this.ri = new BigInteger(256, RANDOM).mod(CURVE.getN());
        this.Pi = CURVE.getG().multiply(ri).normalize();
    }

    public BigInteger getPrivateShare() {
        return ri;
    }

    public ECPoint getPublicShare() {
        return Pi;
    }
}