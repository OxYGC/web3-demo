package com.web3.repository;

import com.web3.entity.AccountAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountAddressRepository extends JpaRepository<AccountAddress, Long> {

    List<AccountAddress> findByAccountId(Long accountId);

    List<AccountAddress> findByAccountIdAndCoinType(Long accountId, String coinType);
}
