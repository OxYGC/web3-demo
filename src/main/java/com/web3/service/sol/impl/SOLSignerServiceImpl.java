package com.web3.service.sol.impl;

import com.web3.service.sol.SolanaSignerService;
import com.web3.util.CryptoUtils;
import org.springframework.stereotype.Service;

/**
 * 签名机实现类，生成 Solana 密钥对
 * 可以在真实环境替换为 HSM / TEE 调用
 */
@Service
public class SOLSignerServiceImpl implements SolanaSignerService {

    @Override
    public KeyPairResult createKeyPair() throws Exception {
        // 使用 CryptoUtils 生成 Solana 密钥对
        String privateKey = CryptoUtils.generatePrivateKey();
        String publicKey = CryptoUtils.derivePublicKey(privateKey);
        String compressedPubKey = CryptoUtils.compressPublicKey(publicKey);

        return new KeyPairResult(privateKey, publicKey, compressedPubKey);
    }
}