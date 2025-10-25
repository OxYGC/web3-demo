package com.web3.entity.vo;

import lombok.Data;

@Data
public class SignMethodResponse {
    private String code;
    private String message;
    private String signMethod;

    public SignMethodResponse(String code, String message, String signMethod) {
        this.code = code;
        this.message = message;
        this.signMethod = signMethod;
    }

    // getter & setter 省略
}