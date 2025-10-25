package com.web3.entity.dto.eth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildAndSignTransactionRequestDTO {

    @NotBlank(message = "交易体不能为空")
    private String txBase64Body; // Base64 编码的交易模板

    @NotBlank(message = "公钥不能为空")
    private String publicKey;    // 对应钱包的公钥，用于获取私钥
}
