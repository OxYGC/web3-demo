package com.web3.service.eth.impl;

import com.web3.entity.dto.eth.CreateKeyPairResponse;
import com.web3.entity.dto.eth.EthereumSchema;
import com.web3.entity.vo.SignedTxResult;
import com.web3.service.eth.EthSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Ethereum 签名机核心服务实现（自包含版）
 * 直接处理密钥生成、签名、交易构建
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EthSignServiceImpl implements EthSignService {
    // 模拟 TEE 内部私钥存储 (可以是levelDB)
    ConcurrentHashMap<String, String> keyStore = new ConcurrentHashMap();

    /**
     * 根据公钥获取私钥
     */
    @Override
    public String getPrivKey(String publicKey) {
        return keyStore.get(publicKey);
    }

    /**
     * 批量生成密钥对
     */
    @Override
    public List<CreateKeyPairResponse> createKeyPairs(int keyNum) {
        List<CreateKeyPairResponse> keyPairs = new ArrayList<>();
        for (int i = 0; i < keyNum; i++) {
            try {
                ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                String privKey = ecKeyPair.getPrivateKey().toString(16);
                String pubKey = ecKeyPair.getPublicKey().toString(16);

                String address = "0x" + Keys.getAddress(ecKeyPair);

                //fixme 模拟持久化存储
                keyStore.put(pubKey, privKey);

                CreateKeyPairResponse keyPairRes = new CreateKeyPairResponse();
                keyPairRes.setPublicKey(pubKey);
                keyPairRes.setAddress(address);
                keyPairs.add(keyPairRes);
            } catch (Exception e) {
                log.error("生成密钥对失败", e);
            }
        }
        return keyPairs;
    }


    /**
     * 解析 Base64 编码的交易模板为 EthereumSchema 对象
     */
    @Override
    public EthereumSchema parseSchema(byte[] decoded) {
        try {
            String json = new String(decoded);
            return EthereumSchema.fromJson(json);
        } catch (Exception e) {
            log.error("解析 EthereumSchema 失败", e);
            throw new RuntimeException("parse schema fail", e);
        }
    }

    /**
     * 构建未签名交易
     */
    @Override
    public byte[] buildUnsignedTx(EthereumSchema schema) {
        try {
            RawTransaction tx = RawTransaction.createTransaction(
                    schema.getNonce(),
                    schema.getGasPrice(),
                    schema.getGasLimit(),
                    schema.getToAddress(),
                    new BigInteger(schema.getAmount()),
                    schema.toJson());
            // 返回未签名交易字节
            return TransactionEncoder.encode(tx);
        } catch (Exception e) {
            log.error("构建未签名交易失败", e);
            throw new RuntimeException("build unsigned tx fail", e);
        }
    }

    /**
     * 使用私钥对交易进行签名
     */
    @Override
    public String signMessage(RawTransaction rawTransaction, Credentials credentials) {
        try {
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            return Base64.getEncoder().encodeToString(signedMessage);
        } catch (Exception e) {
            log.error("交易签名失败", e);
            throw new RuntimeException("sign message fail", e);
        }
    }

    /**
     * 构建签名完成的交易
     */
    @Override
    public SignedTxResult buildSignedTx(EthereumSchema schema, String signature) {
//        try {
//            byte[] sigBytes = Base64.getDecoder().decode(signature);
//            // 这里直接返回已签名交易字节和交易哈希
////            String txHash = Hash.sha3(sigBytes);
////            return new SignedTxResult(sigBytes, txHash);
//        } catch (Exception e) {
//            log.error("构建签名交易失败", e);
//            throw new RuntimeException("build signed tx fail", e);
//        }
//        todo 待填充业务逻辑
        return new SignedTxResult();
    }

}
