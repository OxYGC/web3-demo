package com.web3.util;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Base58;

/**
 * Solana 地址工具类
 * Solana 地址 = 公钥 Base58 编码，这里示例用 Hex/Base58
 */
public class SolanaAddressUtils {

    /**
     * 根据公钥生成 Solana 地址
     * @param pubKeyHex 公钥 Hex 字符串
     * @return Solana 地址
     */
    public static String pubKeyToAddress(String pubKeyHex) throws Exception {
        byte[] pubKeyBytes = Hex.decodeHex(pubKeyHex.toCharArray());
        // Solana 地址通常是 Base58 编码
        return Base58.encode(pubKeyBytes);
    }
}