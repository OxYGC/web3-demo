package com.web3.enums;



public enum SignAlgorithm {
    ECDSA("ecdsa"),
    ED25519("ed25519"),
    SM2("sm2"),
    SCHNORR("schnorr");

    private final String code;

    SignAlgorithm(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isSupported(String code) {
        for (SignAlgorithm alg : values()) {
            if (alg.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}