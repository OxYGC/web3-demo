package com.web3.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class VanityAddressRequest {

    // 兼容旧版：若使用单一规则，可填入 pattern；若使用前后缀，请使用 prefixPattern/suffixPattern
    private String pattern;

    private String prefixPattern; // 新增：前缀规则
    private String suffixPattern; // 新增：后缀规则

    private String addressFormat; // 新增：地址格式，如 BTC: P2PKH/P2SH/BECH32；ETH: EIP55

    private String coinType = "BTC"; // 默认BTC

    @Min(value = 1, message = "生成数量最少为1")
    @Max(value = 20, message = "生成数量最多为20")
    private Integer maxResults = 1; // 默认生成1个

    private String matchType = "CONTAINS"; // CONTAINS, PREFIX, SUFFIX 或 "PREFIX,SUFFIX"
    private String mnemonic;

    String generationType;

    String accountId;
}
