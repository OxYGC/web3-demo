package com.web3.scanner.config;

import org.springframework.stereotype.Component;

// Ethereum 配置
@Component
public class EthereumChainConfig implements ChainConfig {
    public Long getChainId() { return 1L; }
    public String getRpcUrl() { return "https://mainnet.infura.io/v3/..."; }
    public Integer getDecimals(String token) { 
        return "USDT".equals(token) ? 6 : 18; 
    }
    public Long getRequiredConfirmations() { return 12L; }
}

