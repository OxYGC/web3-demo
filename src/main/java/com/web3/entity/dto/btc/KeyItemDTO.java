package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyItemDTO {
    private String privateKey;
    private String publicKey;
}