package com.web3.service;


import com.web3.entity.dto.btc.CreateKeyPairsWithAddressResponseDTO;
import com.web3.entity.dto.btc.PublicKeyWithAddressDTO;
import com.web3.service.btc.SegwitAddressService;
import com.web3.service.btc.TaprootAddressService;
import com.web3.util.PublicKeyUtils;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BtcWalletService {

    private final NetworkParameters params = MainNetParams.get();

    // 假设这是你的签名机或者本地生成Key的Service
    public CreateKeyPairsWithAddressResponseDTO createKeyPairsWithAddresses(int keyNum, String addressFormat) {
        List<PublicKeyWithAddressDTO> retKeyWithAddressList = new ArrayList<>();
        List<KeyItem> keyList = new ArrayList<>();

        for (int i = 0; i < keyNum; i++) {
            // 1. 创建 KeyPair
            // 新生成随机私钥
            ECKey ecKeyPair = new ECKey();
            String privKeyStr = ecKeyPair.getPrivateKeyAsHex();
            String pubKeyStr = ecKeyPair.getPublicKeyAsHex();

            // bitcoinj 默认压缩公钥可用
            String compressPubKeyStr = PublicKeyUtils.compressPublicKey(ecKeyPair.getPrivKey());
            keyList.add(new KeyItem(privKeyStr, pubKeyStr));
            // 2. 生成地址
            String address;
            switch (addressFormat.toLowerCase()) {
                case "p2pkh":
                    address = LegacyAddress.fromKey(params, ecKeyPair).toString();
                    break;
                case "p2wpkh":
                    address = SegwitAddressService.generateP2WPKH(params, ecKeyPair); // 自定义工具方法
                    break;
                case "p2sh":
                    address = SegwitAddressService.generateP2SH(params, ecKeyPair); // 自定义工具方法
                    break;
                case "p2tr":
                    address = TaprootAddressService.generateTaproot(params, ecKeyPair); // 自定义工具方法
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported address type: " + addressFormat);
            }

            retKeyWithAddressList.add(new PublicKeyWithAddressDTO(compressPubKeyStr, pubKeyStr, address));
        }

        // 3. 存储私钥/公钥到安全存储
        boolean isOk = false;
//        todo 数据存储
//        isOk = keyStorageService.storeKeys(keyList);
        if (!isOk) {
            return new CreateKeyPairsWithAddressResponseDTO("ERROR", "Store keys failed", null);
        }

        return new CreateKeyPairsWithAddressResponseDTO("SUCCESS", "Create key pairs with address success", retKeyWithAddressList);
    }

    // 内部类示例，用于存储 Key
    public static class KeyItem {
        private final String privateKey;
        private final String publicKey;

        public KeyItem(String privateKey, String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        // getters
        public String getPrivateKey() { return privateKey; }
        public String getPublicKey() { return publicKey; }
    }
}