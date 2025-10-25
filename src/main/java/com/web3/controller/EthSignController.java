package com.web3.controller;


import com.web3.entity.dto.btc.CreateKeyPairRequestDTO;
import com.web3.entity.dto.eth.*;
import com.web3.entity.vo.BuildAndSignTransactionRequest;
import com.web3.entity.vo.BuildAndSignTransactionResponse;
import com.web3.entity.vo.ChainSchemaResponse;
import com.web3.entity.vo.SignedTxResult;
import com.web3.enums.SignMethodEnum;
import com.web3.service.eth.EthSignService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/eth")
@RequiredArgsConstructor
public class EthSignController {

    @Resource
    private EthSignService ethSignService;

    /**
     * 获取签名方式（固定返回 ecdsa）
     * secp256k1 曲线
     */
    @GetMapping("/signMethod")
    public ChainSignMethodResponse getSignMethod() {
        ChainSignMethodResponse resp = new ChainSignMethodResponse();
        resp.setCode(0);
        resp.setMessage("get sign method success");
        resp.setSignMethod(SignMethodEnum.ECDSA.getMethod());
        return resp;
    }

    /**
     * 获取链的 Schema，用于前端或钱包服务组装交易体
     */
    @GetMapping("/schema")
    public ChainSchemaResponse getSchema() {
        ChainSchemaResponse resp = new ChainSchemaResponse();
        resp.setCode(String.valueOf(0));
        resp.setMessage("get ethereum sign schema success");
        EthereumSchema ethereumSchema = EthereumSchema.buildDefaultSchema();
        resp.setSchema(ethereumSchema.toString());
        return resp;
    }

    /**
     * 创建公私钥对并导出公钥
     */
    @PostMapping("/create-key-list")
    public List<CreateKeyPairResponse> createKeyPairs(@RequestBody CreateKeyPairRequestDTO req) {
        return ethSignService.createKeyPairs(req.getKeyNum());
    }


    /**
     * 构建并签名交易（EIP-1559 动态费用交易）
     */
    @PostMapping("/buildAndSignTransaction")
    public BuildAndSignTransactionResponse buildAndSignTransaction(@RequestBody BuildAndSignTransactionRequest req) {
        BuildAndSignTransactionResponse resp = new BuildAndSignTransactionResponse();

        try {
            // 1. Base64 解码交易体
            byte[] decoded = Base64.getDecoder().decode(req.getTxBase64Body());

            // 2. 解析 JSON 为 EthereumSchema（EIP-1559 动态费交易结构）
            EthereumSchema schema = ethSignService.parseSchema(decoded);

            // 3. 构建原始交易
            RawTransaction rawTx = buildRawTransaction(schema);


            // 4. 根据公钥找到对应的私钥
            String privKey = ethSignService.getPrivKey(req.getPublicKey());
            if (privKey == null) {
                resp.setCode(-1);
                resp.setMessage("get private key by public key fail");
                return resp;
            }

            RawTransaction tx = RawTransaction.createTransaction(
                    schema.getNonce(),
                    schema.getGasPrice(),
                    schema.getGasLimit(),
                    schema.getToAddress(),
                    new BigInteger(schema.getAmount()),
                    schema.toJson());
            Credentials credentials = Credentials.create(privKey, req.getPublicKey());

            // 5. 私钥签名
            String signature = ethSignService.signMessage(tx, credentials);

            // 6. 拼装成完整签名交易
            SignedTxResult signedResult = ethSignService.buildSignedTx(schema, signature);

            // 7. 返回结果
            resp.setCode(0);
            resp.setMessage("sign transaction success");
            resp.setSignedTx(signedResult.getTxHash());

            resp.setTxHash(Numeric.toHexString(Hash.sha3(decoded)));
            resp.setTxMessageHash(Numeric.toHexString(TransactionEncoder.encode(rawTx)));
            return resp;

        } catch (Exception e) {
            log.error("build and sign tx failed", e);
            resp.setCode(-1);
            resp.setMessage("sign transaction fail: " + e.getMessage());
            return resp;
        }
    }

    /**
     * 构建原始交易（EIP-1559 / ERC20 / Legacy）
     */
    private RawTransaction buildRawTransaction(EthereumSchema schema) {
        if (isEthTransfer(schema)) {
            // 普通 ETH 或 ERC20 转账
            byte[] data = buildERC20Data(schema.getToAddress(), new BigInteger(schema.getAmount()));
            String hexDataStr = Numeric.toHexString(data);

            // EIP-1559
            if (schema.getMaxFeePerGas() != null && !schema.getMaxFeePerGas().equals("0")) {
                return RawTransaction.createTransaction(
                        new BigInteger(schema.getNonce() + ""),
                        new BigInteger(schema.getGasPrice() != null ? schema.getGasPrice().toString() : "0"),
                        schema.getGasLimit(),
                        schema.getToAddress(),
                        new BigInteger(schema.getAmount()),
                        hexDataStr
                );
            } else {
                // Legacy
                return RawTransaction.createEtherTransaction(
                        new BigInteger(schema.getNonce() + ""),
                        new BigInteger(schema.getGasPrice() != null ? schema.getGasPrice().toString() : "0"),
                        schema.getGasLimit(),
                        schema.getToAddress(),
                        new BigInteger(schema.getAmount())
                );
            }
        } else {
            // 合约交互 ：主要to的地址是 contractAddress 合约地址
            byte[] data = buildERC20Data(schema.getToAddress(), new BigInteger(schema.getAmount()));
            String hexDataStr = Numeric.toHexString(data);

            return RawTransaction.createTransaction(
                    new BigInteger(schema.getNonce() + ""),
                    new BigInteger(schema.getGasPrice() != null ? schema.getGasPrice().toString() : "0"),
                    schema.getGasLimit(),
                    schema.getContractAddress(),
                    BigInteger.ZERO,
                    hexDataStr
            );
        }
    }


    /**
     * 判断是否是普通 ETH 转账
     */
    private boolean isEthTransfer(EthereumSchema schema) {
        String contract = schema.getContractAddress();
        return contract == null || contract.isEmpty() ||
                contract.equalsIgnoreCase("0x0000000000000000000000000000000000000000") ||
                contract.equalsIgnoreCase("0x00");
    }

    /**
     * 构建 ERC20 transfer(address,uint256) data
     */
    private byte[] buildERC20Data(String toAddress, BigInteger amount) {
        String methodSignature = "transfer(address,uint256)";
        byte[] methodHash = Hash.sha3(methodSignature.getBytes());
        byte[] methodId = new byte[4];
        System.arraycopy(methodHash, 0, methodId, 0, 4);

        byte[] addressBytes = Numeric.hexStringToByteArray(toAddress);
        byte[] paddedAddress = new byte[32];
        System.arraycopy(addressBytes, 0, paddedAddress, 32 - addressBytes.length, addressBytes.length);

        byte[] paddedAmount = Numeric.toBytesPadded(amount, 32);

        byte[] data = new byte[methodId.length + paddedAddress.length + paddedAmount.length];
        System.arraycopy(methodId, 0, data, 0, methodId.length);
        System.arraycopy(paddedAddress, 0, data, methodId.length, paddedAddress.length);
        System.arraycopy(paddedAmount, 0, data, methodId.length + paddedAddress.length, paddedAmount.length);
        return data;
    }


    /**
     * 批量构建并签名交易
     *
     * @param request 批量交易请求
     * @return 批量签名结果
     */
    @PostMapping("/build-sign-batch")
    public ResponseEntity<BuildSignBatchTxRes> buildAndSignBatchTransaction(
            @RequestBody BuildSignBatchTxDTO request) {

        BuildSignBatchTxRes response = new BuildSignBatchTxRes();
        List<SignedTxResultDTO> signedTxList = new ArrayList<>();

        for (BuildAndSignTransactionRequestDTO txReq : request.getTxList()) {
            try {
                // 1. Base64 解码
                byte[] decoded = Base64.getDecoder().decode(txReq.getTxBase64Body());

                // 2. 解析交易模板
                EthereumSchema schema = ethSignService.parseSchema(decoded);

                // 3. 构建原始交易
                RawTransaction rawTx = buildRawTransaction(schema);

                // 4. 获取私钥
                String privKey = ethSignService.getPrivKey(txReq.getPublicKey());
                if (privKey == null) {
                    signedTxList.add(SignedTxResultDTO.fail(txReq.getPublicKey(), "无法获取私钥"));
                    continue;
                }

                // 5. 私钥签名
                RawTransaction tx = RawTransaction.createTransaction(
                        schema.getNonce(),
                        schema.getGasPrice(),
                        schema.getGasLimit(),
                        schema.getToAddress(),
                        new BigInteger(schema.getAmount()),
                        schema.toJson());
                Credentials credentials = Credentials.create(privKey, txReq.getPublicKey());

                String signature = ethSignService.signMessage(tx, credentials);

                // 6. 构建签名完成交易
                byte[] sigBytes = Base64.getDecoder().decode(signature);
                String signedTx = Numeric.toHexString(sigBytes);


                SignedTxResultDTO signedTxResultDTO = new SignedTxResultDTO();

                // 转成字节数组（RLP 编码）
                byte[] encodedTx = TransactionEncoder.encode(rawTx);
                // 转成 hex 字符串（适合 DTO 返回）
                String rawTxHex = Numeric.toHexString(encodedTx);
                signedTxResultDTO.setRawTx(rawTxHex);
                signedTxResultDTO.setSignedTx(signedTx);
                // 计算交易哈希 （Keccak-256 作为统一的 Hash 算法）
                String txHash = Numeric.toHexString(Hash.sha3(sigBytes));
                signedTxResultDTO.setTxHash(txHash);

                signedTxList.add(signedTxResultDTO);
            } catch (Exception e) {
                // 捕获单笔交易异常，不影响其他交易签名
                signedTxList.add(SignedTxResultDTO.fail(txReq.getPublicKey(), e.getMessage()));
            }
        }
        response.setCode(0);
        response.setMessage("批量签名完成");
        response.setSignedTxList(signedTxList);
        return ResponseEntity.ok(response);
    }


}


// 入参数
/*
{
  "code": "SUCCESS",
  "message": "create keys with address success",
  "public_key_addresses": [
    {
      "public_key": "04bc07a90250df4be52d23310d34cde52d57f4d95f9826cedb594cf3ae002f285ab9708f3303ba526fb3bcb68896568922ef6673ea64e4c49ac57a9c784cfdb811",
      "compress_public_key": "03bc07a90250df4be52d23310d34cde52d57f4d95f9826cedb594cf3ae002f285a",
      "address": "0x360Ccc76AE5DA207d0960cF81f2589d9d4d5F26D"
    },
    {
      "public_key": "042db546d77bf9427b9dd6b2ce1ce5343ab7c83aa70d73f3472a61b8abe972665d5b3aaf73c75555728547c7adbe3d069139b8d423a4ecf8d9d3f83e30088da05f",
      "compress_public_key": "032db546d77bf9427b9dd6b2ce1ce5343ab7c83aa70d73f3472a61b8abe972665d",
      "address": "0x86C79442fAEce848b194050Bb1dEDD0b8Cad7487"
    }
  ]
}

{
    "chain_id": "11155111",
    "nonce": 0,
    "from_address": "0x360Ccc76AE5DA207d0960cF81f2589d9d4d5F26D",
    "to_address": "0x45Bd8ea16cFEB0D937a2D98cBEb0300e3E689Fe7",
    "gas_limit": 21000,
    "gas": 2000000,
    "max_fee_per_gas": "327993150328",
    "max_priority_fee_per_gas": "32799315032",
    "amount": "100000000000000000",
    "contract_address": "0x00"
}
*/
