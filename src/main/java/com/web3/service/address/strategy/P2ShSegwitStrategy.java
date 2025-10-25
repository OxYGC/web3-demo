package com.web3.service.address.strategy;

import com.web3.entity.AddressInfo;
import com.web3.service.address.IAddressGenerationStrategy;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.springframework.stereotype.Service;

/**
 * P2SH(P2WPKH) (兼容型) 实现 (m/49'/coin'/acct'/change/index)
 * 先生成 witness pubkey hash, 然后包装成 P2SH scriptPubKey
 */
@Service
public class P2ShSegwitStrategy implements IAddressGenerationStrategy {
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

        // create witness program (P2WPKH) and embed to P2SH
        Script segwitScript = ScriptBuilder.createP2WPKHOutputScript(ecKey);
        Script p2sh = ScriptBuilder.createP2SHOutputScript(segwitScript);
        Address addr = LegacyAddress.fromScriptHash(params, ScriptPattern.extractHashFromP2SH(p2sh));
        String pubHex = ecKey.getPublicKeyAsHex();
        String path = child.getPathAsString();
        return new AddressInfo(chainId, addr.toString(), pubHex, path, index);
    }
}
