package com.web3.service.btc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web3.entity.dto.btc.BitcoinSchema;
import com.web3.entity.dto.btc.Vin;
import com.web3.entity.dto.btc.Vout;
import com.web3.util.AddrOutScriptParser;
import jakarta.annotation.Resource;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private KeyStoreService keyStoreService;


    // parse schema bytes -> BitcoinSchema
    public BitcoinSchema parseSchema(byte[] jsonBytes) throws Exception {
        return objectMapper.readValue(jsonBytes, BitcoinSchema.class);
    }


    /**
     * 简化实现：将 vins/vouts 序列化为 JSON（确定性顺序）后做 double-sha256 作为“待签名消息哈希”。
     * 真实场景：需要实现比特币的 sighash 算法（BIP143/BTC legacy 等）；这里用于示例和 TEE 测试。
     */
    public byte[] calcSignHashes(List<Vin> vins, List<Vout> vouts) throws Exception {
        // 1. 构造 bitcoinj Transaction
        NetworkParameters params = MainNetParams.get();
        Transaction tx = new Transaction(params);

        // 1) 添加输入（注意：scriptSig 这里先留空，签名时会填充）
        for (Vin vin : vins) {
            // prev tx hash
            Sha256Hash prevHash = Sha256Hash.wrap(vin.getTxid()); // txid hex -> Sha256Hash
            // 构造 outpoint：构造器为 TransactionOutPoint(long index, Sha256Hash hash)
//            TransactionOutPoint outPoint = new TransactionOutPoint(vin.getVout(), prevHash);
            TransactionOutPoint outPoint = new TransactionOutPoint(params,vin.getVout(), prevHash);

            //  scriptBytes 先给空（签名前不会用到此 scriptSig）
            byte[] emptyScript = new byte[0];
//            TransactionInput input = new TransactionInput(tx, emptyScript, outPoint, vin.getSequence());
            TransactionInput input = new TransactionInput(params,tx, emptyScript, outPoint);
            tx.addInput(input);
        }

        // 添加输出
        for (Vout vout : vouts) {
            Coin amount = Coin.valueOf(vout.getValue());
            Script script = new Script(Hex.decode(vout.getScriptPubKeyHex()));
            tx.addOutput(new TransactionOutput(params, tx, amount, script.getProgram()));
        }

        // 2. 遍历每个输入，计算 sigHash
        for (int i = 0; i < vins.size(); i++) {
            Vin vin = vins.get(i);
            Script.ScriptType type = AddrOutScriptParser.detectType(vin.getScriptPubKey());

            switch (type) {
                case P2PKH:
                    return calcP2PKHSigHash(tx, i, vin.getScriptPubKey());
                case P2WPKH:
                    return calcP2WPKHSigHash(tx, i, vin.getScriptPubKey(), vin.getValue());
                case P2TR:
                    return calcTaprootSigHash(tx, i, vin.getScriptPubKey(), vin.getValue());
                default:
                    throw new UnsupportedOperationException("Unsupported script type: " + type);
            }
        }
        return null;
    }


    /**
     * P2PKH 签名哈希计算（bitcoinj 原生支持）
     */
    public byte[] calcP2PKHSigHash(Transaction tx, int vinIndex, String scriptPubKeyHex) throws Exception {
        Script scriptPubKey = new Script(Hex.decode(scriptPubKeyHex));

        Sha256Hash sigHash = tx.hashForSignature(
                vinIndex,
                scriptPubKey,
                Transaction.SigHash.ALL,
                false
        );

        return sigHash.getBytes();
    }


    /**
     * P2WPKH 签名哈希计算（bitcoinj 需要 BIP143）
     */
    public byte[] calcP2WPKHSigHash(Transaction tx, int vinIndex, String scriptPubKeyHex, long amount) throws Exception {
        Script scriptCode = new Script(Hex.decode(scriptPubKeyHex));

        Sha256Hash sigHash = tx.hashForWitnessSignature(
                vinIndex,
                scriptCode,
                Coin.valueOf(amount),
                Transaction.SigHash.ALL,
                false
        );

        return sigHash.getBytes();
    }


    /**
     * Taproot (P2TR) 签名哈希（bitcoinj 暂不支持，需要手写 BIP341 算法，这里留个 TODO）
     */
    public byte[] calcTaprootSigHash(Transaction tx, int vinIndex, String scriptPubKeyHex, long amount) {
        throw new UnsupportedOperationException("Taproot sighash not implemented");
    }


    /**
     * 从 KeyStore 获取私钥（ECKey），并返回签名（DER + sighashAll appended）hex 字符串
     *
     * @param pubKeyHex 公钥 hex 用于查私钥
     * @param msg       待签名哈希（32 bytes），如果不是 32 bytes，本方法会对其做 SHA256
     * @return signature hex (DER + sighash byte)
     */
    public String signMessage(String pubKeyHex, byte[] msg) throws Exception {
        ECKey ecKey = keyStoreService.findECKeyByPubHex(pubKeyHex);
        if (ecKey == null) {
            throw new IllegalStateException("private key not found for publicKey: " + pubKeyHex);
        }

        // 如果 msg 不是 32 字节则对其做 double-sha256（或单sha256），这里用 Sha256Hash.wrap 需要 32 byte
        Sha256Hash hash;
        if (msg.length == 32) {
            hash = Sha256Hash.wrap(msg);
        } else {
            byte[] h = Sha256Hash.hash(msg);
            hash = Sha256Hash.wrap(h);
        }

        // ECDSA 签名（secp256k1）
        ECKey.ECDSASignature sig = ecKey.sign(hash);
        // 转成 DER
        byte[] der = sig.encodeToDER();

        // 在比特币交易签名里需要在 DER 后面追加 sighash type (0x01 = SIGHASH_ALL)
        byte sighashAll = 0x01;
        byte[] derPlus = new byte[der.length + 1];
        System.arraycopy(der, 0, derPlus, 0, der.length);
        derPlus[der.length] = sighashAll;

        return Hex.toHexString(derPlus);
    }

    public String getPrivKey(String publicKey) {
        ECKey ecKeyByPubHex = keyStoreService.findECKeyByPubHex(publicKey);
        return ecKeyByPubHex.getPrivateKeyAsHex();
    }
}