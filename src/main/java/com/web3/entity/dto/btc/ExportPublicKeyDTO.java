package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportPublicKeyDTO {
    private String publicKey;
    private String compressedPublicKey;
}
