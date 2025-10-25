package com.web3.repository;

import com.web3.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountId(String accountId);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.addresses ORDER BY a.createdAt DESC")
    List<Account> findAllWithAddresses();

    boolean existsByAccountId(String accountId);
}
