package com.web3.entity.vo;

import lombok.Data;

@Data
public class ChainSchemaResponse {
    private String code;
    private String message;
    private String schema;

    public ChainSchemaResponse() {
    }

    public ChainSchemaResponse(String code, String message, String schema) {
        this.code = code;
        this.message = message;
        this.schema = schema;
    }

    // getters & setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
