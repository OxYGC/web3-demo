package com.web3.entity.dto.sol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个公钥与地址信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyWithAddressDTO {
    private String publicKey;
    private String compressedPubKey;
    private String address;
}
