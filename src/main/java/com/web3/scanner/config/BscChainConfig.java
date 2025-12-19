package com.web3.scanner.config;

import org.springframework.stereotype.Component;

// BSC 配置
@Component
public class BscChainConfig implements ChainConfig {
    public Long getChainId() { return 56L; }
    public String getRpcUrl() { return "https://bsc-dataseed.binance.org"; }
    public Integer getDecimals(String token) { 
        return 18; // BSC 上 USDT 是 18 位
    }
    public Long getRequiredConfirmations() { return 10L; }
}