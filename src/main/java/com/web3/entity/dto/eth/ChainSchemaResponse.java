package com.web3.entity.dto.eth;

import lombok.Data;

@Data
public class ChainSchemaResponse {
    private int code;
    private String message;
    private String schema; // JSON 字符串
}