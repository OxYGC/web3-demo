package com.web3.entity.dto.sol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 响应对象：批量生成密钥返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKeyPairsResponseDTO {
    private String code; // SUCCESS / ERROR
    private String message;
    private List<com.web3.entity.dto.sol.PublicKeyWithAddressDTO> keyList;
}