package com.web3.service.address;

import com.web3.entity.AddressInfo;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;

public interface IAddressGenerationStrategy {
    AddressInfo deriveAddress(NetworkParameters params, DeterministicHierarchy dh, DeterministicKey accountKey, int change, int index, String chainId);

}
