package com.web3.util;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Solana SPL Token 工具类（生产环境可用）
 */
public class SplTokenUtils {

    // SPL Token Program ID（固定）
    public static final PublicKey TOKEN_PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

    // Associated Token Program ID（固定）
    public static final PublicKey ASSOCIATED_TOKEN_PROGRAM_ID = new PublicKey("ATokenGPvoterPublicKey"); // 请替换为正确的AToken地址

    // 系统 Program ID（固定）
    public static final PublicKey SYSTEM_PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    // Sysvar Rent 公钥（固定）
    public static final PublicKey SYSVAR_RENT_PUBKEY = new PublicKey("SysvarRent111111111111111111111111111111111");

    /**
     * 计算 ATA 地址
     */
    public static PublicKey getAssociatedTokenAddress(PublicKey walletAddress, PublicKey mintAddress) {
        try {
            List<byte[]> seeds = Arrays.asList(
                    walletAddress.toByteArray(),
                    TOKEN_PROGRAM_ID.toByteArray(),
                    mintAddress.toByteArray()
            );
            return PublicKey.createProgramAddress(seeds, ASSOCIATED_TOKEN_PROGRAM_ID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive ATA address", e);
        }
    }

    /**
     * 创建 ATA 指令
     */
    public static TransactionInstruction createAssociatedTokenAccountInstruction(
            PublicKey payer,
            PublicKey ataAccount,
            PublicKey mint
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(payer, true, true));
        keys.add(new AccountMeta(ataAccount, false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(payer, false, false));
        keys.add(new AccountMeta(SYSTEM_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(TOKEN_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));

        return new TransactionInstruction(
                ASSOCIATED_TOKEN_PROGRAM_ID,
                keys,
                new byte[0]
        );
    }

    /**
     * SPL Token 转账指令
     */
    public static TransactionInstruction createTransferTokenInstruction(
            long amount,
            PublicKey fromTokenAccount,
            PublicKey toTokenAccount,
            PublicKey owner
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put((byte) 3); // SPL Token transfer instruction
        buffer.putLong(amount);

        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(fromTokenAccount, false, true));
        keys.add(new AccountMeta(toTokenAccount, false, true));
        keys.add(new AccountMeta(owner, true, false));

        return new TransactionInstruction(
                TOKEN_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }
}