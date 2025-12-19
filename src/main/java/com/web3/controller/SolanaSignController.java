package com.web3.controller;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.web3.entity.dto.btc.CreateKeyPairRequestDTO;
import com.web3.entity.dto.eth.ChainSignMethodResponse;
import com.web3.entity.dto.sol.CreateKeyPairsResponseDTO;
import com.web3.entity.dto.sol.PublicKeyWithAddressDTO;
import com.web3.entity.dto.sol.SolBuildSignTxDTO;
import com.web3.entity.dto.sol.SolanaSchema;
import com.web3.entity.vo.BuildAndSignTransactionResponse;
import com.web3.enums.SignMethodEnum;
import com.web3.service.btc.KeyStoreService;
import com.web3.service.btc.SignerService;
import com.web3.service.sol.SolanaSignerService;
import com.web3.service.sol.WalletDbService;
import com.web3.util.SolanaAddressUtils;
import com.web3.util.SplTokenUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.SystemProgram;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/sol")
@RequiredArgsConstructor
public class SolanaSignController {

    @Resource
    private SolanaSignerService solanaSignerService;
    @Resource
    private WalletDbService walletDbService;
    @Resource
    private KeyStoreService keyStoreService;

    ConcurrentHashMap<String, String> keyStore = new ConcurrentHashMap<>();

    /**
     * 获取签名方式（固定返回 ecdsa）
     * secp256k1 曲线
     */
    @GetMapping("/sign-method")
    public ChainSignMethodResponse getSignMethod() {
        ChainSignMethodResponse resp = new ChainSignMethodResponse();
        resp.setCode(0);
        resp.setMessage("get sign method success");
        resp.setSignMethod(SignMethodEnum.EDDSA.getMethod());
        return resp;
    }

    /**
     * 获取链的 Schema，用于前端或钱包服务组装交易体
     */
    @GetMapping("/schema")
    public ResponseEntity<SolanaSchema> getChainSchema() {
        // 构建默认模板
        SolanaSchema schema = SolanaSchema.buildDefaultSchema();
        return ResponseEntity.ok(schema);
    }


    /**
     * 创建公私钥对并导出公钥，携带地址
     */
    @PostMapping("/create-key-list")
    public ResponseEntity<CreateKeyPairsResponseDTO> createKeyPairs(@RequestBody CreateKeyPairRequestDTO request) {
        CreateKeyPairsResponseDTO response = new CreateKeyPairsResponseDTO();
        response.setCode("ERROR");


        int keyNum = request.getKeyNum();
        if (keyNum <= 0 || keyNum > 10000) {
            response.setMessage("keyNum must be between 1 and 10000");
            return ResponseEntity.badRequest().body(response);
        }

        List<WalletDbService.KeyItem> keyItems = new ArrayList<>();
        List<PublicKeyWithAddressDTO> keyDtos = new ArrayList<>();

        for (int i = 0; i < keyNum; i++) {
            try {
                // 1. 生成密钥对
                SolanaSignerService.KeyPairResult keyPair = solanaSignerService.createKeyPair();

                // 2. 存储数据库对象
                WalletDbService.KeyItem keyItem = new WalletDbService.KeyItem();
                keyItem.setPrivateKey(keyPair.getPrivateKey());
                keyItem.setPubKey(keyPair.getPublicKey());
                keyItems.add(keyItem);

                // 3. 根据公钥生成 Solana 地址
                String address = SolanaAddressUtils.pubKeyToAddress(keyPair.getPublicKey());

                // 4. 返回给钱包层对象
                PublicKeyWithAddressDTO dto = new PublicKeyWithAddressDTO();
                dto.setPublicKey(keyPair.getPublicKey());
//                dto.setCompressPublicKey(keyPair.getCompressedPubKey());
                dto.setAddress(address);
                keyDtos.add(dto);

            } catch (Exception e) {
                response.setMessage("Failed to create key pair: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }

        // 5. 批量存储到数据库
        boolean stored = walletDbService.storeKeys(keyItems);
        if (!stored) {
            response.setMessage("Failed to store keys in database");
            return ResponseEntity.internalServerError().body(response);
        }

        // 6. 返回成功结果
        response.setCode("SUCCESS");
        response.setMessage("Keys created successfully");
        response.setKeyList(keyDtos);
        return ResponseEntity.ok(response);
    }

    @Resource
    private SignerService signerService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 构建并签名一笔 Solana 交易
     */
    @PostMapping("/build-and-sign")
    public BuildAndSignTransactionResponse buildAndSignTransaction(@RequestBody SolBuildSignTxDTO req) throws IOException {
        BuildAndSignTransactionResponse resp = new BuildAndSignTransactionResponse();
        resp.setCode(-1);

        // 1. 解析 Base64 交易数据
        byte[] decoded = Base64.getDecoder().decode(req.getTxBase64Body());
        SolanaSchema schema = objectMapper.readValue(decoded, SolanaSchema.class);

        // 2. 解析 from/to 公钥
        PublicKey fromPub = new PublicKey(schema.getFromAddress());
        PublicKey toPub = new PublicKey(schema.getToAddress());

        // 3. 转账金额
        long lamports;
        try {
            lamports = Long.parseLong(schema.getValue());
        } catch (NumberFormatException e) {
            resp.setMessage("Invalid value field in schema");
            return resp;
        }

        // 4. 构建交易指令列表
        List<TransactionInstruction> instructions = new ArrayList<>();

        if (isSolTransfer(schema)) {
            // SOL 转账
            instructions.add(SystemProgram.transfer(fromPub, toPub, lamports));
        } else {
            // 1. 构建 Mint 公钥
            PublicKey mintPub = new PublicKey(schema.getContractAddress());

            // 2. 计算发送方和接收方的 ATA 地址
            PublicKey fromTokenAccount = SplTokenUtils.getAssociatedTokenAddress(fromPub, mintPub);
            PublicKey toTokenAccount = SplTokenUtils.getAssociatedTokenAddress(toPub, mintPub);

            // 3. 判断是否需要创建 ATA，如果需要则加入创建 ATA 的指令
            if (schema.isTokenCreate()) {
                TransactionInstruction createATAInstr = SplTokenUtils.createAssociatedTokenAccountInstruction(fromPub, toTokenAccount, mintPub);
                instructions.add(createATAInstr);
            }

            // 4. 构建 SPL Token 转账指令
            TransactionInstruction transferInstr = SplTokenUtils.createTransferTokenInstruction(lamports, fromTokenAccount, toTokenAccount, fromPub);
            instructions.add(transferInstr);
        }

        // 5. 构建交易
        Transaction tx = new Transaction();
        instructions.forEach(tx::addInstruction);
        //该值由钱包层传入
        tx.setRecentBlockHash(req.getRecentBlockhash());
//        tx.setFeePayer(fromPub);

        // 6. 获取私钥 自己实现的 TEE 内部获取私钥方法

        //todo 该处模拟数据
        String privKey = keyStore.get(req.getPublicKey());

        if (privKey == null) {
            resp.setMessage("get private key fail");
            return resp;
        }

        Account fromAccount = new Account(privKey.getBytes());
        tx.sign(fromAccount);

        // 7. 序列化交易
        byte[] serializedTx = tx.serialize();
        String base58Tx = Base58.encode(serializedTx);
        resp.setSignedTx(base58Tx);

        // 8. 返回
        resp.setCode(200);
        resp.setMessage("sign transaction success");
        // 原始消息 hash

        byte[] rawMessageBytes = tx.serialize(); // 完整交易序列化
        resp.setTxMessageHash(Base58.encode(rawMessageBytes));
        resp.setTxHash(Numeric.toHexString(decoded));
        return resp;
    }

    private boolean isSolTransfer(SolanaSchema s) {
        String c = s.getContractAddress();
        return c == null || c.isEmpty() || c.equals("0x") || c.equals("0x00");
    }

    // 你需要实现：根据 publicKeyId 查找私钥 bytes（在 TEE/KeyStore/LevelDB/KeyVault）
    private byte[] fetchSecretKeyBytesByPublicKeyId(String publicKeyId) {
        // 示例：假设在你的 DB 里以 hex 保存 64 bytes 私钥（secretKey = 64 bytes）
        String hex = /* 查询 DB */ "";
        return hexStringToBytes(hex);
    }

    private static byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 判断是否为 SOL 转账（即没有 contractAddress 或 contractAddress=0x00）
     */
    private boolean isSolTransfer(String contractAddress) {
        return contractAddress == null || contractAddress.isEmpty() || contractAddress.equals("0x0000000000000000000000000000000000000000") || contractAddress.equals("0x00");
    }


//    public static PublicKey getAssociatedTokenAddress(PublicKey walletAddress, PublicKey mintAddress) {
//        PublicKey associatedTokenProgramId = new PublicKey("ATokenGPvoterPublicKey"); // 或者官方 SPL Token Program Id
//        PublicKey derived = PublicKey.createProgramAddress(Arrays.asList(walletAddress.toByteArray(), TokenProgram.PROGRAM_ID.toByteArray(), mintAddress.toByteArray()), associatedTokenProgramId);
//        return derived;
//    }


}


