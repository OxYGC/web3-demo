package com.web3.service.address.strategy;

import com.web3.entity.AddressInfo;
import com.web3.service.address.IAddressGenerationStrategy;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.*;
import org.springframework.stereotype.Service;

/**
 * Legacy P2PKH 实现 (m/44'/coin'/acct'/change/index)
 */
@Service
public class LegacyP2PKHStrategy implements IAddressGenerationStrategy {
    @Override
    public AddressInfo deriveAddress(NetworkParameters params, DeterministicHierarchy dh, DeterministicKey accountKey, int change, int index, String chainId) {
        // 1) 先派生 change 节点 (外部链: change=0; change地址一般是 0)
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(change, false));

        // 2) 再派生 index 节点
        DeterministicKey child = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(index, false));

        // 3) 从 child 得到 ECKey（有私钥则用私钥，否则用公钥）
        ECKey ecKey;
        if (child.hasPrivKey()) {
            ecKey = ECKey.fromPrivate(child.getPrivKeyBytes());
        } else {
            // watch-only
            ecKey = ECKey.fromPublicOnly(child.getPubKey());
        }
        // 4) 生成 P2PKH 地址（legacy）
        Address addr = LegacyAddress.fromKey(params, ecKey);

        String pubHex = ecKey.getPublicKeyAsHex();  // 或 ecKey.getPublicKeyAsHex()
        String path = child.getPathAsString();

        return new AddressInfo(chainId, addr.toString(), pubHex, path, index);



    }
}
