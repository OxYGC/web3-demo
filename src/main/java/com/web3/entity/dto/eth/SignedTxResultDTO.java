package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单笔签名交易结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedTxResultDTO {
    private String publicKey;      // 公钥
    private String txHash;         // 交易哈希
    private String signedTx;       // 完整签名后的交易序列化 Hex
    private String rawTx;          // 待签名交易 Hash/Raw
    private boolean success;       // 签名是否成功
    private String message;        // 错误信息或成功提示

    public static SignedTxResultDTO fail(String publicKey, String message) {
        SignedTxResultDTO result = new SignedTxResultDTO();
        result.setPublicKey(publicKey);
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static SignedTxResultDTO success(String publicKey, String txHash, String signedTx, String rawTx) {
        SignedTxResultDTO result = new SignedTxResultDTO();
        result.setPublicKey(publicKey);
        result.setTxHash(txHash);
        result.setSignedTx(signedTx);
        result.setRawTx(rawTx);
        result.setSuccess(true);
        result.setMessage("签名成功");
        return result;
    }
}
