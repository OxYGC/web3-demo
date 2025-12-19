package com.web3.service.btc.impl;

import com.web3.entity.dto.btc.KeyItemDTO;
import com.web3.service.btc.KeyStoreService;
import org.bitcoinj.core.ECKey;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyStoreServiceImpl implements KeyStoreService {

    @Override
    public boolean storeKeys(List<KeyItemDTO> keyList) {
        return false;
    }

    @Override
    public ECKey findECKeyByPubHex(String pubKeyHex) {
        return null;
    }

}
