package com.web3.util;


import org.bitcoinj.script.Script;

/**
 * 地址生成格式解析器
 * BTC地址脚本解析
 */
public class AddrOutScriptParser {

    /**
     * 根据 scriptPubKey 的 HEX 判断交易输出类型
     *
     * @param scriptPubKeyHex 脚本（16进制字符串）
     * @return ScriptType（P2PKH, P2WPKH, P2SH, P2TR 等）
     */
    public static Script.ScriptType detectType(String scriptPubKeyHex) {

        if (scriptPubKeyHex == null || scriptPubKeyHex.isEmpty()) {
            throw new IllegalArgumentException("scriptPubKey is empty");
        }

        String hex = scriptPubKeyHex.toLowerCase();

        // P2PKH: OP_DUP OP_HASH160 <20-byte pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
        // 76a914{20-byte-hash}88ac
        if (hex.startsWith("76a914") && hex.endsWith("88ac") && hex.length() == 50) {
            return Script.ScriptType.P2PKH;
        }

        // P2SH: OP_HASH160 <20-byte scriptHash> OP_EQUAL
        // a914{20-byte-hash}87
        if (hex.startsWith("a914") && hex.endsWith("87") && hex.length() == 46) {
            return Script.ScriptType.P2SH;
        }

        // P2WPKH: 0x00 + PUSH(20) + pubKeyHash
        // 0014{20-byte-hash}
        if (hex.startsWith("0014") && hex.length() == 44) {
            return Script.ScriptType.P2WPKH;
        }

        // P2WSH: 0x00 + PUSH(32) + scriptHash
        // 0020{32-byte-hash}
        if (hex.startsWith("0020") && hex.length() == 68) {
            return Script.ScriptType.P2WSH;
        }

        // P2TR (Taproot): OP_1 (0x51) + PUSH(32) + x-only-pubKey
        // 5120{32-byte-pubkey}
        if (hex.startsWith("5120") && hex.length() == 68) {
            return Script.ScriptType.P2TR;
        }
        //todo 这里做异常unknown处理
        return Script.ScriptType.valueOf(scriptPubKeyHex);
//        return ScriptType.UNKNOWN;
    }
}