package com.web3.entity.dto.sol;

import lombok.Data;

@Data
public class SolBuildSignTxDTO {
    private String txBase64Body;
    private String publicKey;

    private String recentBlockhash;


}