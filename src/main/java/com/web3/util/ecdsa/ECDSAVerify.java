package com.web3.util.ecdsa;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.crypto.ec.CustomNamedCurves;

import java.math.BigInteger;

public final class ECDSAVerify {

    // secp256k1 曲线参数
    private static final X9ECParameters CURVE =
            CustomNamedCurves.getByName("secp256k1");

    // ECDSA Domain Parameters（⚠️ 必须用这个）
    private static final ECDomainParameters DOMAIN =
            new ECDomainParameters(
                    CURVE.getCurve(),
                    CURVE.getG(),
                    CURVE.getN(),
                    CURVE.getH()
            );

    private ECDSAVerify() {
    }

    /**
     * 验证 ECDSA 签名
     *
     * @param hash      已经 hash 过的消息（32 bytes）
     * @param r         签名 r
     * @param s         签名 s（建议已经是 low-s）
     * @param publicKey 公钥点（secp256k1）
     */
    public static boolean verify(
            byte[] hash,
            BigInteger r,
            BigInteger s,
            ECPoint publicKey
    ) {
        if (hash == null || hash.length == 0) {
            throw new IllegalArgumentException("hash is empty");
        }
        if (r == null || s == null) {
            throw new IllegalArgumentException("signature is null");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey is null");
        }

        ECDSASigner verifier = new ECDSASigner();

        verifier.init(
                false,
                new ECPublicKeyParameters(publicKey, DOMAIN)
        );

        return verifier.verifySignature(hash, r, s);
    }
}