package com.web3.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class MnemonicGenerateRequest {

    @Min(value = 12, message = "助记词长度最少12个单词")
    @Max(value = 24, message = "助记词长度最多24个单词")
    private Integer wordCount = 12; // 默认12个单词

    private String language = "english"; // 默认英文

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
