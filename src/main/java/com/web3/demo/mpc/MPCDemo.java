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
}