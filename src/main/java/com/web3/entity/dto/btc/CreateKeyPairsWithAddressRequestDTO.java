package com.web3.entity.dto.btc;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateKeyPairsWithAddressRequestDTO {
    @Min(1)
    @Max(10000)
    private int keyNum;

    /**
     *
     * 地址生成类型 (OutputScriptType)
     *
     * eg: p2pkh, p2wpkh, p2sh, p2tr
     */
    @NotBlank
    private String addressFormat;
}