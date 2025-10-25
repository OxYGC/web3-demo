package com.web3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class SignResponse {
    private String chainId;
    private String keyId;
    private String signature;
}