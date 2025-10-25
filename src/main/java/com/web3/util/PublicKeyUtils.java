package com.web3.util;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 *
 * 压缩公钥相关工具类
 *
 * 例如：
 * 压缩公钥: 0x0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798
 * 解压公钥: 0x04...（65字节）
 * 未压缩公钥: 0x04...（65字节）
 */

public class PublicKeyUtils {

    private static final ECDomainParameters CURVE;

    static {
        var params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    /**
     * 压缩公钥 (压缩成 33 字节，02/03 + X)
     *
     * @param publicKey 公钥大整数 (通常来自 ECKeyPair.getPublicKey())
     * @return 压缩公钥的 hex 字符串 (0x 前缀)
     */
    public static String compressPublicKey(BigInteger publicKey) {
        byte[] pubKeyBytes = Numeric.toBytesPadded(publicKey, 64);

        // 拼装 uncompressed 格式：0x04 + X(32字节) + Y(32字节)
        byte[] encoded = new byte[65];
        encoded[0] = 0x04;
        System.arraycopy(pubKeyBytes, 0, encoded, 1, pubKeyBytes.length);

        // 使用椭圆曲线解码 & 压缩
        ECPoint point = CURVE.getCurve().decodePoint(encoded);
        return Numeric.toHexString(point.getEncoded(true)); // true = compressed
    }

    /**
     * 解压缩公钥 (从 33字节恢复到 65字节 04+X+Y)
     *
     * @param compressedPubKey 压缩公钥 (hex 字符串)
     * @return 未压缩公钥的 hex 字符串 (0x04 + X + Y)
     */
    public static String decompressPublicKey(String compressedPubKey) {
        ECPoint point = CURVE.getCurve().decodePoint(Numeric.hexStringToByteArray(compressedPubKey));
        return Numeric.toHexString(point.getEncoded(false)); // false = uncompressed
    }

    /**
     * 从 ECKeyPair 生成压缩公钥
     *
     * @param keyPair ECKeyPair (web3j 生成的密钥对)
     * @return 压缩公钥 hex 字符串
     */
    public static String getCompressedPublicKey(ECKeyPair keyPair) {
        return compressPublicKey(keyPair.getPublicKey());
    }

    /**
     * 从 ECKeyPair 生成未压缩公钥
     *
     * @param keyPair ECKeyPair
     * @return 未压缩公钥 hex 字符串
     */
    public static String getUncompressedPublicKey(ECKeyPair keyPair) {
        byte[] pubKeyBytes = Numeric.toBytesPadded(keyPair.getPublicKey(), 64);

        byte[] encoded = new byte[65];
        encoded[0] = 0x04;
        System.arraycopy(pubKeyBytes, 0, encoded, 1, pubKeyBytes.length);

        return Numeric.toHexString(encoded);
    }
}