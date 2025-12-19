package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量生成密钥对 + 地址响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKeyPairsWithAddressResponse {
    private int code;
    private String message;
    private List<ExportPublicKeyWithAddress> publicKeyAddresses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportPublicKeyWithAddress {
        private String publicKey;
        private String compressedPubKey;
        private String address;
    }
}