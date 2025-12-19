package com.web3.demo.ecdsa.mpc;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 签名验签
 * MPC签名Demo
 * x: 私钥
 * k:签名的随机数(k每次签名都不同，保证签名唯一)
 * R = k*G (生成签名的点,决定r值，r = R.x mod n)
 */
public class MPCSigningDemo {

    public static void main(String[] args) {

        // 3 节点
        MPCNode n1 = new MPCNode();
        MPCNode n2 = new MPCNode();
        MPCNode n3 = new MPCNode();

        List<MPCNode> nodes = List.of(n1, n2, n3);

        // 聚合公钥
        ECPoint aggregatedPublicKey = MPCManager.aggregatePublicKeys(List.of(n1.getPublicShare(), n2.getPublicShare(), n3.getPublicShare()));

        String message = "Hello MPC";

        //	1 表示 正数符号（signum = 1）目的是保证 hash 被解释为正整数，不会因为最高位是 1 被误认为负数，固定写死为1
        BigInteger hash = new BigInteger(1, message.getBytes()); // 简化 hash
        // 模拟 Threshold ECDSA 签名
        BigInteger n = MPCNode.CURVE.getN();

        // 每个节点生成随机 k_i
        List<BigInteger> kList = new ArrayList<>();
        for (MPCNode node : nodes) {
            kList.add(new BigInteger(256, new SecureRandom()).mod(n));
        }

        // 聚合 k = sum(k_i) mod n
        BigInteger k = BigInteger.ZERO;
        for (BigInteger ki : kList) {
            k = k.add(ki).mod(n);
        }

        // 计算 R = k * G
        ECPoint R = MPCNode.CURVE.getG().multiply(k).normalize();
        BigInteger r = R.getAffineXCoord().toBigInteger();

        // 每个节点计算部分 s_i = k_i^{-1} * (hash + r * x_i) mod n
        BigInteger s = BigInteger.ZERO;
        for (int i = 0; i < nodes.size(); i++) {
            BigInteger xi = nodes.get(i).getPrivateShare();
            BigInteger ki = kList.get(i);
            BigInteger si = ki.modInverse(n).multiply(hash.add(r.multiply(xi))).mod(n);
            s = s.add(si).mod(n);
        }

        System.out.println("Aggregated Public Key: " + aggregatedPublicKey);
        System.out.println("Signature (r, s): (" + r + ", " + s + ")");
    }
}