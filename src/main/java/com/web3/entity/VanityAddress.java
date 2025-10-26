package com.web3.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vanity_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VanityAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "private_key", columnDefinition = "TEXT", nullable = false)
    private String privateKey;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "coin_type", nullable = false)
    private String coinType;

    @Column(name = "pattern", nullable = false)
    private String pattern;

    @Column(name = "match_type")
    private String matchType; // PREFIX, SUFFIX, CONTAINS

    @Column(name = "generation_time")
    private Long generationTime; // 生成耗时（毫秒）

    @Column(name = "account_id")
    private String accountId; // 关联账户ID

    @Column(name = "task_id")
    private String taskId; // 生成任务ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public VanityAddress(String address, String privateKey, String publicKey, String coinType, 
                        String pattern, String matchType, Long generationTime) {
        this.address = address;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.coinType = coinType;
        this.pattern = pattern;
        this.matchType = matchType;
        this.generationTime = generationTime;
    }

    public VanityAddress(String address, String privateKey, String publicKey, String coinType, 
                        String pattern, String matchType, Long generationTime, String accountId, String taskId) {
        this.address = address;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.coinType = coinType;
        this.pattern = pattern;
        this.matchType = matchType;
        this.generationTime = generationTime;
        this.accountId = accountId;
        this.taskId = taskId;
    }
}
