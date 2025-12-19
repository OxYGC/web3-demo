package com.web3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BalanceRequest {
    
    @NotBlank(message = "地址不能为空")
    private String address;
    
    @NotBlank(message = "币种类型不能为空")
    private String coinType; // BTC, ETH
    
    private String network = "mainnet"; // mainnet, testnet
}
