package com.web3.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "blockchain.networks")
@Data
@Slf4j
public class NetworkConfig {

    private Map<String, NetworkInfo> btc = new HashMap<>();
    private Map<String, NetworkInfo> eth = new HashMap<>();
    private Map<String, CustomNetwork> custom = new HashMap<>();

    @Data
    public static class NetworkInfo {
        private String name;
        private String displayName;
        private String apiUrl;
        private String apiKey;
        private boolean enabled = true;
        private String explorerUrl;
        private String description;
    }

    @Data
    public static class CustomNetwork {
        private String name;
        private String displayName;
        private String coinType;
        private String apiUrl;
        private String apiKey;
        private String explorerUrl;
        private String description;
        private boolean enabled = true;
    }

    /**
     * 获取BTC网络配置
     */
    public NetworkInfo getBtcNetwork(String network) {
        return btc.get(network);
    }

    /**
     * 获取ETH网络配置
     */
    public NetworkInfo getEthNetwork(String network) {
        return eth.get(network);
    }

    /**
     * 获取自定义网络配置
     */
    public CustomNetwork getCustomNetwork(String networkId) {
        return custom.get(networkId);
    }

    /**
     * 获取所有可用的BTC网络
     */
    public Map<String, NetworkInfo> getAvailableBtcNetworks() {
        Map<String, NetworkInfo> available = new HashMap<>();
        btc.forEach((key, value) -> {
            if (value.isEnabled()) {
                available.put(key, value);
            }
        });
        return available;
    }

    /**
     * 获取所有可用的ETH网络
     */
    public Map<String, NetworkInfo> getAvailableEthNetworks() {
        Map<String, NetworkInfo> available = new HashMap<>();
        eth.forEach((key, value) -> {
            if (value.isEnabled()) {
                available.put(key, value);
            }
        });
        return available;
    }

    /**
     * 获取所有可用的自定义网络
     */
    public Map<String, CustomNetwork> getAvailableCustomNetworks() {
        Map<String, CustomNetwork> available = new HashMap<>();
        custom.forEach((key, value) -> {
            if (value.isEnabled()) {
                available.put(key, value);
            }
        });
        return available;
    }

    /**
     * 添加自定义网络
     */
    public void addCustomNetwork(String networkId, CustomNetwork network) {
        custom.put(networkId, network);
        log.info("添加自定义网络: {} - {}", networkId, network.getDisplayName());
    }

    /**
     * 移除自定义网络
     */
    public void removeCustomNetwork(String networkId) {
        custom.remove(networkId);
        log.info("移除自定义网络: {}", networkId);
    }

    /**
     * 验证网络是否存在且可用
     */
    public boolean isNetworkAvailable(String coinType, String network) {
        switch (coinType.toUpperCase()) {
            case "BTC":
                NetworkInfo btcNetwork = getBtcNetwork(network);
                return btcNetwork != null && btcNetwork.isEnabled();
            case "ETH":
                NetworkInfo ethNetwork = getEthNetwork(network);
                return ethNetwork != null && ethNetwork.isEnabled();
            default:
                CustomNetwork customNetwork = getCustomNetwork(network);
                return customNetwork != null && customNetwork.isEnabled() && 
                       coinType.equalsIgnoreCase(customNetwork.getCoinType());
        }
    }
}
