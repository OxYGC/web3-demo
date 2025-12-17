package com.web3.demo.mpc;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import org.bouncycastle.math.ec.ECPoint;

public class MPCDemo {

    public static void main(String[] args) {

        // 3 个 MPC 节点
        MPCNode n1 = new MPCNode();
        MPCNode n2 = new MPCNode();
        MPCNode n3 = new MPCNode();

        List<ECPoint> publicShares = new ArrayList<>();
        publicShares.add(n1.getPublicShare());
        publicShares.add(n2.getPublicShare());
        publicShares.add(n3.getPublicShare());

        // =======================
        // 聚合公钥
        // =======================
        ECPoint aggregatedPublicKey = MPCManager.aggregatePublicKeys(publicShares);
        System.out.println("Aggregated Public Key:");
        System.out.println(aggregatedPublicKey);

        // =======================
        // 聚合私钥
        // =======================
        BigInteger aggregatedPrivateKey = n1.getPrivateShare()
                .add(n2.getPrivateShare())
                .add(n3.getPrivateShare())
                .mod(MPCNode.CURVE.getN()); // 模 N 聚合

        System.out.println("Aggregated Private Key:");
        System.out.println(aggregatedPrivateKey.toString(16));

        // 用聚合私钥生成公钥
        ECPoint derivedPublicKey = MPCNode.CURVE.getG().multiply(aggregatedPrivateKey).normalize();
        System.out.println("Derived Public Key from Aggregated Private Key:");
        System.out.println(derivedPublicKey);

        // 验证是否一致
        if (derivedPublicKey.equals(aggregatedPublicKey)) {
            System.out.println("✅ 聚合公钥与聚合私钥推算的公钥一致");
        } else {
            System.out.println("❌ 不一致");
        }
    }


    /**
     * 打印输出(x,y,z坐标是“内部状态”，对协议安全性没有直接作用，但对性能至关重要, 坐标是“内部状态”，对协议安全性没有直接作用，但对性能至关重要,Z 坐标记录了比例关系，最后统一归一化得到标准公钥)
     * Aggregated Public Key:
     * (908faa6e3c001e586a3be84751a2c5c43121b627add3cb19d881c24f62de027d,151d375c499d9067950792897362174f67bbda08facaa794765421cabd5ad673,1)
     * Aggregated Private Key:
     * 8a9c521a84732d500fcba714fcd811c40c214ec4bae22f79718e1a12b3c72dd5
     * Derived Public Key from Aggregated Private Key:
     * (908faa6e3c001e586a3be84751a2c5c43121b627add3cb19d881c24f62de027d,151d375c499d9067950792897362174f67bbda08facaa794765421cabd5ad673,1)
     * ✅ 聚合公钥与聚合私钥推算的公钥一致
     */

}