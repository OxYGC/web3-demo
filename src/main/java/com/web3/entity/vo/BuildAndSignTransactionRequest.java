package com.web3.entity.vo;

import lombok.Data;

@Data
public class BuildAndSignTransactionRequest {
    private String txBase64Body;
    private String publicKey;
}