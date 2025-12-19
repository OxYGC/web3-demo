package com.web3.dto;

public enum VanityTaskStatus {
    PENDING("待开始"),
    RUNNING("运行中"), 
    PAUSED("已暂停"),
    COMPLETED("已完成"),
    STOPPED("已停止"),
    ERROR("出错");

    private final String description;

    VanityTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
