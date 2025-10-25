package com.web3.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyPairResult {
    private String privateKeyHex;
    private BigInteger privateKey;
    private String wif;
    private String publicKey;
    private String compressPublicKey;


    // getter / setter
}