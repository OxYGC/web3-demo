package com.web3.entity.vo;

import lombok.Data;

@Data
public class BuildAndSignTransactionResponse {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int code;
    private String message;
    private String txMessageHash;
    private String txHash;
    private String signedTx;
}