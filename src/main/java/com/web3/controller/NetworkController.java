package com.web3.controller;

import com.web3.config.NetworkConfig;
import com.web3.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
@Slf4j
public class NetworkController {

    private final NetworkConfig networkConfig;

    /**
     * 获取所有可用网络配置
     */
    @GetMapping("/available")
    public ApiResponse<Map<String, Object>> getAvailableNetworks() {
        try {
            Map<String, Object> networks = new HashMap<>();
            networks.put("btc", networkConfig.getAvailableBtcNetworks());
            networks.put("eth", networkConfig.getAvailableEthNetworks());
            networks.put("custom", networkConfig.getAvailableCustomNetworks());
            
            return ApiResponse.success("获取网络配置成功", networks);
        } catch (Exception e) {
            log.error("获取网络配置失败", e);
            return ApiResponse.error("获取网络配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定币种的网络配置
     */
    @GetMapping("/{coinType}")
    public ApiResponse<Map<String, ?>> getNetworksByCoinType(@PathVariable String coinType) {
        try {
            Map<String, ?> networks;
            switch (coinType.toUpperCase()) {
                case "BTC":
                    networks = networkConfig.getAvailableBtcNetworks();
                    break;
                case "ETH":
                    networks = networkConfig.getAvailableEthNetworks();
                    break;
                default:
                    return ApiResponse.error("不支持的币种: " + coinType);
            }
            
            return ApiResponse.success("获取" + coinType + "网络配置成功", networks);
        } catch (Exception e) {
            log.error("获取{}网络配置失败", coinType, e);
            return ApiResponse.error("获取网络配置失败: " + e.getMessage());
        }
    }

    /**
     * 添加自定义网络
     */
    @PostMapping("/custom")
    public ApiResponse<String> addCustomNetwork(@Valid @RequestBody CustomNetworkRequest request) {
        try {
            NetworkConfig.CustomNetwork customNetwork = new NetworkConfig.CustomNetwork();
            customNetwork.setName(request.getName());
            customNetwork.setDisplayName(request.getDisplayName());
            customNetwork.setCoinType(request.getCoinType());
            customNetwork.setApiUrl(request.getApiUrl());
            customNetwork.setApiKey(request.getApiKey());
            customNetwork.setExplorerUrl(request.getExplorerUrl());
            customNetwork.setDescription(request.getDescription());
            customNetwork.setEnabled(true);
            
            String networkId = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
            networkConfig.addCustomNetwork(networkId, customNetwork);
            
            return ApiResponse.success("自定义网络添加成功", networkId);
        } catch (Exception e) {
            log.error("添加自定义网络失败", e);
            return ApiResponse.error("添加自定义网络失败: " + e.getMessage());
        }
    }

    /**
     * 删除自定义网络
     */
    @DeleteMapping("/custom/{networkId}")
    public ApiResponse<String> removeCustomNetwork(@PathVariable String networkId) {
        try {
            networkConfig.removeCustomNetwork(networkId);
            return ApiResponse.success("自定义网络删除成功", networkId);
        } catch (Exception e) {
            log.error("删除自定义网络失败", e);
            return ApiResponse.error("删除自定义网络失败: " + e.getMessage());
        }
    }

    /**
     * 验证网络连接
     */
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateNetwork(@RequestBody NetworkValidationRequest request) {
        try {
            boolean isValid = networkConfig.isNetworkAvailable(request.getCoinType(), request.getNetwork());
            return ApiResponse.success("网络验证完成", isValid);
        } catch (Exception e) {
            log.error("网络验证失败", e);
            return ApiResponse.error("网络验证失败: " + e.getMessage());
        }
    }

    // 内部类：自定义网络请求
    public static class CustomNetworkRequest {
        private String name;
        private String displayName;
        private String coinType;
        private String apiUrl;
        private String apiKey;
        private String explorerUrl;
        private String description;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }
        
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getExplorerUrl() { return explorerUrl; }
        public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // 内部类：网络验证请求
    public static class NetworkValidationRequest {
        private String coinType;
        private String network;

        // Getters and Setters
        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }
        
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
    }
}
