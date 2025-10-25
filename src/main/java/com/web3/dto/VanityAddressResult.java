package com.web3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VanityAddressResult {
    private String address;
    private String privateKey;
    private String publicKey;
    private String coinType;
    private String pattern;
}
