package com.web3.scanner.config;

public interface ChainConfig {
    Long getChainId();
    String getRpcUrl();
    Integer getDecimals(String tokenSymbol);
    // 如 USDT 在 BSC 是 18，ETH 是 6
    Long getRequiredConfirmations();
}