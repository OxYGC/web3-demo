package com.web3.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web3.entity.dto.btc.*;
import com.web3.entity.vo.*;
import com.web3.enums.SignAlgorithm;
import com.web3.service.BtcWalletService;
import com.web3.service.btc.KeyStoreService;
import com.web3.service.btc.SignService;
import com.web3.service.btc.SignerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * 比特币支持的相关算饭
 */
@Slf4j
@RestController
@RequestMapping("/btc")
public class BtcController {

    @Resource
    private KeyStoreService keyStoreService;
    @Resource
    private SignerService signerService;
    @Resource
    private BtcWalletService btcWalletService;


    /**
     * 1. 获取支持的签名算法
     */
    @GetMapping("/sign-method")
    public ResponseEntity<SignMethodResponse> getSignMethod() {
        SignMethodResponse response = new SignMethodResponse(
                "SUCCESS",
                "Get sign method success",
                SignAlgorithm.ECDSA.getCode()  // 可以改成动态返回多个算法
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 交易签名所需的数据结构模板
     * @return
     */
    @GetMapping("/chain-schema")
    public ResponseEntity<ChainSchemaResponse> getChainSchema() {
        try {
            // 构造示例 Vin / Vout
            Vin vin = new Vin().getDefault();

            Vout vout = new Vout("", 0, 0L);

            BitcoinSchema schema = new BitcoinSchema(
                    "0",           // requestId
                    "0",           // fee
                    Collections.singletonList(vin),
                    Collections.singletonList(vout)
            );

            // 返回 JSON 字符串
            ObjectMapper mapper = new ObjectMapper();
            String schemaJson = mapper.writeValueAsString(schema);
            ChainSchemaResponse response = new ChainSchemaResponse(
                    "SUCCESS",
                    "get bitcoin sign schema success",
                    schemaJson);

            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            ChainSchemaResponse response = new ChainSchemaResponse(
                    "FAIL",
                    "marshal fail: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * 通常使用这个(当有些算法升级或者不支持的时候，使用/batch-create方法)
     * 批量生成密钥对并导出公钥和地址
     *
     * @param request 包含生成数量和地址格式
     * @return 响应体：生成的公钥和地址列表
     */
    @PostMapping("/batch-create-with-address")
    public ResponseEntity<CreateKeyPairsWithAddressResponseDTO> createKeyPairsWithAddresses(
            @RequestBody CreateKeyPairsWithAddressRequestDTO request) {
        CreateKeyPairsWithAddressResponseDTO response =
                btcWalletService.createKeyPairsWithAddresses(request.getKeyNum(), request.getAddressFormat());
        return ResponseEntity.ok(response);
    }


    /**
     * 批量生成公钥 (该方法不生成地址)
     * @param request
     * @return
     */
    @PostMapping("/batch-create")
    public ResponseEntity<CreateKeyPairResponseDTO> createKeyPairsAndExportPublicKeys(
            @RequestBody CreateKeyPairRequestDTO request) {

        CreateKeyPairResponseDTO response = new CreateKeyPairResponseDTO();
        if (request.getKeyNum() > 10000) {
            response.setCode("ERROR");
            response.setMessage("Number of keys must be less than 10000");
            return ResponseEntity.badRequest().body(response);
        }

        List<KeyItemDTO> keyList = new ArrayList<>();
        List<ExportPublicKeyDTO> publicKeyList = new ArrayList<>();

        for (int i = 0; i < request.getKeyNum(); i++) {
            try {
                KeyPairResult keyPair = signerService.createKeyPair();

                KeyItemDTO keyItem = new KeyItemDTO(keyPair.getPrivateKeyHex(), keyPair.getPublicKey());
                ExportPublicKeyDTO pubKeyItem = new ExportPublicKeyDTO(keyPair.getPublicKey(), keyPair.getCompressPublicKey());

                keyList.add(keyItem);
                publicKeyList.add(pubKeyItem);

            } catch (Exception e) {
                response.setCode("ERROR");
                response.setMessage("Failed to create key pairs: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }

        boolean stored = keyStoreService.storeKeys(keyList);
        if (!stored) {
            response.setCode("ERROR");
            response.setMessage("Failed to store generated keys");
            return ResponseEntity.internalServerError().body(response);
        }

        response.setCode("SUCCESS");
        response.setMessage("Successfully created keys");
        response.setPublicKeyList(publicKeyList);

        return ResponseEntity.ok(response);
    }






    @Resource
    private SignService signService;


    /**
     * 构建交易签名：
     * 	•	数字资产转账
     * 	•	多签流程
     * 	•	跨链桥、托管钱包
     * 	•	交易构建与广播
     *
     *
     * @param req
     * @return
     */
    @PostMapping("/build-tx-sign")
    public BuildAndSignTransactionResponse buildAndSignTransaction(@RequestBody BuildAndSignTransactionRequest req) {

        BuildAndSignTransactionResponse resp = new BuildAndSignTransactionResponse();
        resp.setCode(BuildAndSignTransactionResponse.ERROR);

        try {
            // 1. Base64 解析
            byte[] decoded = Base64.getDecoder().decode(req.getTxBase64Body());

            // 2. 解析 JSON 到 BitcoinSchema
            BitcoinSchema schema = signService.parseSchema(decoded);

            // 3. 计算签名Hash (sigHash)
            byte[] buf = signService.calcSignHashes(schema.getVins(), schema.getVouts());

            // 4. Get private key by public key
            String privKey = signService.getPrivKey(req.getPublicKey());
            if (privKey == null) {
                resp.setMessage("get private key by public key fail");
                return resp;
            }

            // 5. 进行签名
            String signature = signService.signMessage(privKey, buf);

            log.info("calc sign hash success, buf={}", buf);

            resp.setCode(BuildAndSignTransactionResponse.SUCCESS);
            resp.setMessage("sign tx success");
            resp.setTxMessageHash(signature);
            // TODO: 如果后续需要算 TxHash，可以加
            resp.setTxHash("");
            // TODO: 如果需要完整序列化交易，可以加
            resp.setSignedTx("");
            return resp;

        } catch (Exception e) {
            log.error("buildAndSignTransaction error", e);
            resp.setMessage("sign transaction fail: " + e.getMessage());
            return resp;
        }
    }













}

