package com.web3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicKeyResponse {
    private String chainId;
    private String keyId;
    private String publicKey;
}
