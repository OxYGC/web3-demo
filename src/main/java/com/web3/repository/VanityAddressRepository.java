package com.web3.repository;

import com.web3.entity.VanityAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VanityAddressRepository extends JpaRepository<VanityAddress, Long> {

    List<VanityAddress> findByCoinTypeOrderByCreatedAtDesc(String coinType);

    List<VanityAddress> findByPatternOrderByCreatedAtDesc(String pattern);

    @Query("SELECT v FROM VanityAddress v ORDER BY v.createdAt DESC")
    List<VanityAddress> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(v) FROM VanityAddress v WHERE v.coinType = ?1")
    Long countByCoinType(String coinType);
}
