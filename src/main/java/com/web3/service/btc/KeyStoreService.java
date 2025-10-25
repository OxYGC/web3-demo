package com.web3.service.btc;

import com.web3.entity.dto.btc.KeyItemDTO;
import org.bitcoinj.crypto.ECKey;

import java.util.List;

public interface KeyStoreService {
    boolean storeKeys(List<KeyItemDTO> keyList);

    ECKey findECKeyByPubHex(String pubKeyHex);
}
