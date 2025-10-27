package com.web3.service.btc;


import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.springframework.stereotype.Service;

@Service
public class SegwitAddressService {

    /**
     * 生成 P2WPKH 地址（native SegWit）
     */
    public static String generateP2WPKH(NetworkParameters params, ECKey key) {
        return SegwitAddress.fromKey(params, key).toString();
    }

    /**
     * 生成 P2SH-P2WPKH 地址（兼容 SegWit）
     */
    public static String generateP2SH(NetworkParameters params, ECKey key) {
        // 创建 P2WPKH 脚本
//        byte[] witnessProgram = key.getPubKeyHash();
        org.bitcoinj.script.Script segwitScript = org.bitcoinj.script.ScriptBuilder.createP2WPKHOutputScript(key);
        LegacyAddress p2shAddress = LegacyAddress.fromScriptHash(params, segwitScript.getProgram());
        return p2shAddress.toString();
    }
}