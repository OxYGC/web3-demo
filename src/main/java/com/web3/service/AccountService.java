package com.web3.service;

import com.web3.dto.WalletInfo;
import com.web3.entity.Account;
import com.web3.entity.AccountAddress;
import com.web3.repository.AccountRepository;
import com.web3.repository.AccountAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountAddressRepository accountAddressRepository;
    private final WalletService walletService;

    /**
     * 创建新账号并生成地址
     */
    @Transactional
    public Map<String, Object> createAccount(String type) {
        // 生成账号ID
        String accountId = generateAccountId();
        // 直接使用ID作为显示名称
        String accountName = accountId;

        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountName(accountName);

        String mnemonic = null;
        String privateKey = null;

        // 根据类型生成助记词或私钥
        if ("mnemonic".equals(type)) {
            mnemonic = walletService.generateMnemonic(12);
            account.setMnemonic(mnemonic);
        } else if ("privatekey".equals(type)) {
            privateKey = walletService.generatePrivateKey();
            account.setPrivateKey(privateKey);
        }

        // 保存账号
        account = accountRepository.save(account);

        // 生成默认地址
        generateDefaultAddresses(account, mnemonic, privateKey);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("accountName", accountName);
        result.put("type", type);
        if (mnemonic != null) {
            result.put("mnemonic", mnemonic);
        }
        if (privateKey != null) {
            result.put("privateKey", privateKey);
        }

        return result;
    }

    /**
     * 生成默认地址
     */
    private void generateDefaultAddresses(Account account, String mnemonic, String privateKey) {
        List<String> blockchains = Arrays.asList("BTC", "ETH");
        List<WalletInfo> wallets;

        if (mnemonic != null) {
            wallets = walletService.generateWalletsFromMnemonic(mnemonic, blockchains, 2, 0);
        } else {
            wallets = walletService.generateWalletsFromPrivateKey(privateKey, blockchains);
        }

        List<AccountAddress> addresses = new ArrayList<>();
        for (WalletInfo wallet : wallets) {
            if (wallet != null) {
                AccountAddress address = new AccountAddress(
                    wallet.getBlockchain(),
                    wallet.getAddress(),
                    wallet.getIndex(),
                    wallet.getDerivationPath(),
                    account
                );
                addresses.add(address);
            }
        }

        accountAddressRepository.saveAll(addresses);
    }

    /**
     * 获取所有账号列表
     */
    public List<Map<String, Object>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAllWithAddresses();

        return accounts.stream().map(account -> {
            Map<String, Object> accountMap = new HashMap<>();
            accountMap.put("account", account.getAccountId());
            accountMap.put("account_name", account.getAccountName());

            // 按币种分组地址
            Map<String, List<String>> addressesByCoins = new HashMap<>();
            if (account.getAddresses() != null) {
                for (AccountAddress addr : account.getAddresses()) {
                    addressesByCoins.computeIfAbsent(addr.getCoinType(), k -> new ArrayList<>())
                                   .add(addr.getAddress());
                }
            }
            accountMap.put("addresses", addressesByCoins);

            return accountMap;
        }).collect(Collectors.toList());
    }

    /**
     * 生成账号ID
     */
    private String generateAccountId() {
        String accountId;
        do {
            accountId = "ACC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
        } while (accountRepository.existsByAccountId(accountId));
        return accountId;
    }

    /**
     * 检查账号是否存在
     */
    public boolean accountExists(String accountId) {
        return accountRepository.existsByAccountId(accountId);
    }

    /**
     * 删除账号（级联删除地址）
     */
    @Transactional
    public boolean deleteAccount(String accountId) {
        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        accountRepository.delete(accountOpt.get());
        return true;
    }

    /**
     * 将靓号地址关联到账户
     */
    @Transactional
    public void addVanityAddressToAccount(String accountId, com.web3.entity.VanityAddress vanityAddress) {
        // 找到对应账户
        Optional<Account> account = accountRepository.findByAccountId(accountId);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在: " + accountId);
        }

        // 创建账户地址记录
        AccountAddress accountAddress = new AccountAddress();
        accountAddress.setCoinType(vanityAddress.getCoinType());
        accountAddress.setAddress(vanityAddress.getAddress());
        accountAddress.setAddressIndex(999); // 靓号地址使用特殊索引
        accountAddress.setDerivationPath("vanity:" + vanityAddress.getPattern()); // 特殊标记
        accountAddress.setAccount(account.get());

        accountAddressRepository.save(accountAddress);

        log.info("成功将靓号地址 {} 关联到账户 {}", vanityAddress.getAddress(), accountId);
    }
}
