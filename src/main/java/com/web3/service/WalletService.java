package com.web3.service;


import com.web3.entity.AddressInfo;
import com.web3.service.address.IAddressGenerationStrategy;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * WalletService: 基于 mnemonic/seed 批量派生地址（线程安全）
 * <p>
 * - 支持不同的 derivation base path (accountKey) 例如 BIP44, BIP49, BIP84
 * - 使用 DeterministicHierarchy 来派生 child key
 */
public class WalletService {
    private final NetworkParameters params;
    private final DeterministicHierarchy dh;
    private final DeterministicKey rootKey; // master xprv
    private final String chainId;


    public WalletService(NetworkParameters params, DeterministicKey rootKey, DeterministicHierarchy dh, String chainId) {
        this.params = params;
        this.rootKey = rootKey;
        this.dh = dh;
        this.chainId = chainId;
    }




    /**
     * Derive an account node for a given purpose and coinType and account (BIP44/BIP84/BIP49)
     * return DeterministicKey for m / purpose' / coin' / account'
     */
    public DeterministicKey getAccountKey(int purpose, int coinType, int account) {
        // build path m / purpose' / coinType' / account'
        List<ChildNumber> acctPath = Arrays.asList(
                new ChildNumber(purpose, true),
                new ChildNumber(coinType, true),
                new ChildNumber(account, true)
        );
        return dh.get(acctPath, false, true);
    }

    /**
     * Batch generate addresses for a given accountKey and strategy.
     * change: 0 = external, 1 = change
     */
    public List<AddressInfo> generateAddresses(IAddressGenerationStrategy strategy,
                                               DeterministicKey accountKey,
                                               int change, int startIndex, int count) {
        List<AddressInfo> out = new ArrayList<>(count);
        for (int i = startIndex; i < startIndex + count; i++) {
            AddressInfo info = strategy.deriveAddress(params, dh, accountKey, change, i, chainId);
            out.add(info);
        }
        return out;
    }

    // Factory helpers
    public static WalletService createFromMnemonic(String mnemonic, String passphrase, NetworkParameters params, String chainId) throws UnreadableWalletException {
        long creationTimeSeconds = System.currentTimeMillis() / 1000L;
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, creationTimeSeconds);
        // use seed to create root private key
        DeterministicKey rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        DeterministicHierarchy dh = new DeterministicHierarchy(rootKey);
        return new WalletService(params, rootKey, dh, chainId);
    }
}