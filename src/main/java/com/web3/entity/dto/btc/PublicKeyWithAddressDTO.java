package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyWithAddressDTO {
    private String compressPublicKey;
    private String publicKey;
    private String address;
}