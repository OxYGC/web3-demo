package com.web3.service.btc.impl;

import com.web3.entity.vo.KeyPairResult;
import com.web3.service.btc.SignerService;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.springframework.stereotype.Service;

@Service
public class SignerServiceImpl implements SignerService {

    private final NetworkParameters params = MainNetParams.get(); // BTC 主网


    @Override
    public KeyPairResult createKeyPair() throws Exception {
        // 1. 生成新的私钥和公钥
        ECKey key = new ECKey();
        // 2. 获取私钥 WIF（Wallet Import Format）或十六进制
        String privateKeyHex = key.getPrivateKeyAsHex();
        String wif = key.getPrivateKeyAsWiF(params);

        // 3. 获取压缩公钥和非压缩公钥
        String pubKeyHex = key.getPublicKeyAsHex();

        String compressPubKeyHex = key.getPubKey().length == 33 ? pubKeyHex : null;
        // 4. 返回封装对象
        return new KeyPairResult(privateKeyHex,key.getPrivKey(), wif, pubKeyHex, compressPubKeyHex);
    }
}
