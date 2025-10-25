package com.web3.entity.dto.btc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateKeyPairsWithAddressResponseDTO {
    private String code;
    private String message;
    private List<PublicKeyWithAddressDTO> publicKeyAddresses;
}