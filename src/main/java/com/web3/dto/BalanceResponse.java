package com.web3.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceResponse {
    
    private String address;
    private String coinType;
    private String balance; // 余额字符串，避免精度问题
    private String balanceFormatted; // 格式化后的余额显示
    private String unit; // 单位：BTC, ETH, Satoshi, Wei等
    private Long confirmations; // 确认数
    private String network; // 网络类型
    private Long timestamp; // 查询时间戳
    private boolean success; // 查询是否成功
    private String errorMessage; // 错误信息
}
