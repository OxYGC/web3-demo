package com.web3.entity.dto.btc;

import lombok.Data;

import java.util.List;

@Data
public class BitcoinSchema {
    private String requestId;
    private String fee;
    private List<Vin> vins;
    private List<Vout> vouts;







    public BitcoinSchema() {
    }

    public BitcoinSchema(String requestId, String fee, List<Vin> vins, List<Vout> vouts) {
        this.requestId = requestId;
        this.fee = fee;
        this.vins = vins;
        this.vouts = vouts;
    }

    // getters & setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public List<Vin> getVins() {
        return vins;
    }

    public void setVins(List<Vin> vins) {
        this.vins = vins;
    }

    public List<Vout> getVouts() {
        return vouts;
    }

    public void setVouts(List<Vout> vouts) {
        this.vouts = vouts;
    }
}
