package com.web3.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SignMethodEnum {
    ECDSA("ecdsa", new String[]{"BTC", "ETH", "BNB"}),
    EDDSA("eddsa", new String[]{"SOL", "ADA", "DOT", "NEAR"}),
    SM2("sm2", new String[]{"BSN", "FISCO"}),
    BLS("bls", new String[]{"ETH2"}),
    // 几乎不用在链上
    RSA("rsa", new String[]{});

    private final String method;
    private final Set<String> supportedTokens;

    SignMethodEnum(String method, String[] tokens) {
        this.method = method;
        this.supportedTokens = new HashSet<>(Arrays.asList(tokens));
    }

    public String getMethod() {
        return method;
    }

    public boolean supports(String token) {
        return supportedTokens.contains(token.toUpperCase());
    }

    public static SignMethodEnum fromToken(String token) {
        for (SignMethodEnum sm : values()) {
            if (sm.supports(token)) {
                return sm;
            }
        }
        throw new IllegalArgumentException("Unsupported token: " + token);
    }
}