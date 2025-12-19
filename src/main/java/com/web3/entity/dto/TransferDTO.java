package com.web3.entity.dto;

import lombok.Data;

import java.nio.channels.Channels;

@Data
public class TransferDTO {
    /**
     * 私钥
     */
    private String privateKey;

    private String from;

    private String toAddress;
    private  Channels channels;




}
