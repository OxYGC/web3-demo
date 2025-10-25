package com.web3.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class VanityAddressRequest {

    @NotBlank(message = "靓号模式不能为空")
    private String pattern;

    private String coinType = "BTC"; // 默认BTC

    @Min(value = 1, message = "生成数量最少为1")
    @Max(value = 20, message = "生成数量最多为20")
    private Integer maxResults = 1; // 默认生成1个

    private String matchType = "CONTAINS"; // CONTAINS, PREFIX, SUFFIX
    private String mnemonic;

    String generationType;

    String accountId;


}
