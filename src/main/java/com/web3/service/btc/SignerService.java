package com.web3.service.btc;


import com.web3.entity.vo.KeyPairResult;

public interface SignerService {

    KeyPairResult createKeyPair() throws Exception;
}