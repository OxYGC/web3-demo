package com.web3.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "account_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_type", nullable = false)
    private String coinType;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_index")
    private Integer addressIndex;

    @Column(name = "derivation_path")
    private String derivationPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    public AccountAddress(String coinType, String address, Integer addressIndex, String derivationPath, Account account) {
        this.coinType = coinType;
        this.address = address;
        this.addressIndex = addressIndex;
        this.derivationPath = derivationPath;
        this.account = account;
    }
}
