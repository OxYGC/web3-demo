package com.web3.entity;


import com.web3.dao.PrivateKeyWrapper;

public interface KeyStore {

    void saveKey(String keyId, PrivateKeyWrapper privateKey);

    PrivateKeyWrapper getKey(String keyId);

    void removeKey(String keyId);

    boolean containsKey(String keyId);
}