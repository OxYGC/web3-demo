package com.web3.service.eth;

import com.web3.entity.dto.eth.CreateKeyPairResponse;
import com.web3.entity.dto.eth.EthereumSchema;

import com.web3.entity.vo.SignedTxResult;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;

import java.util.List;

public interface EthSignService {
    /**
     * 根据公钥获取私钥
     */
    String getPrivKey(String publicKey);


    List<CreateKeyPairResponse> createKeyPairs(int keyNum);

//    CreateKeyPairsWithAddressResponse createKeyPairsWithAddress(int keyNum);

    EthereumSchema parseSchema(byte[] decoded);

    byte[] buildUnsignedTx(EthereumSchema schema);

    String signMessage(RawTransaction rawTransaction, Credentials credentials);




    SignedTxResult buildSignedTx(EthereumSchema schema, String signature);




}