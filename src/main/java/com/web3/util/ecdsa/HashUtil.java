package com.web3.util.ecdsa;

import org.bouncycastle.jcajce.provider.digest.Keccak;

public class HashUtil {
    public static byte[] keccak256(byte[] input) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        return digest.digest(input);
    }
}