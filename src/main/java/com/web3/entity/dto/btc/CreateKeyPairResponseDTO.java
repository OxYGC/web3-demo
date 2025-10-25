package com.web3.entity.dto.btc;

import lombok.Data;

import java.util.List;

@Data
public class CreateKeyPairResponseDTO {
    private String code;
    private String message;
    private List<ExportPublicKeyDTO> publicKeyList;
}
