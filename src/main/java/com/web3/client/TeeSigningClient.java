package com.web3.client;

/*
TEE Signing Machine — Java 客户端示例
功能：
 1) 使用 mTLS 与 Signing Service 通信
 2) 获取 Enclave Quote (/attestation/quote)
 3) 将 Quote 提交到 Attestation Verifier（示例：内部 verifier 或 Intel IAS），并获取 short-lived JWT
 4) 使用 JWT 或 mTLS 调用 /sign，获取签名与 quote metadata
 5) 获取公钥 /pubkey，并用 BouncyCastle 验证 ECDSA_secp256k1 签名

依赖（Maven）:

<dependencies>
  <!-- JSON -->
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
  </dependency>

  <!-- BouncyCastle for secp256k1 support -->
  <dependency>
    <groupId>org.bouncycastle</groupId>
    <a
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.16.0</version>
  </dependency>
</dependencies>
rtifactId>bcprov-jdk15on</artifactId>
    <version>1.71</version>
  </dependency>

  <!-- Optional: Apache Commons Codec for hex/base64 utilities -->
  <dependency>
说明：
 - 请将 keystore (client cert+key) 与 truststore (CA certs) 预先准备好。
 - 下面示例采用 Java 11+ HttpClient。
 - attestationVerifierUrl 可以是你内部的 verifier 服务，也可以是桥接到 Intel IAS/DCAP 的服务。
*/


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Map;

public class TeeSigningClient {
    private final HttpClient client;
    // TEE 签名机服务 的访问地址
    private final String baseUrl; // e.g. https://signing-service.internal

    // 远程证明（attestation）验证服务 的地址
    private final String attestationVerifierUrl; // e.g. https://attest-verifier.internal/verify
    private final ObjectMapper mapper = new ObjectMapper();




    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public TeeSigningClient(String baseUrl, String attestationVerifierUrl, String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) throws Exception {
        this.baseUrl = baseUrl;
        this.attestationVerifierUrl = attestationVerifierUrl;
        this.client = buildMutualTlsClient(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword);
    }

    private HttpClient buildMutualTlsClient(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            keyStore.load(fis, keyStorePassword.toCharArray());
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream(trustStorePath)) {
            trustStore.load(fis, trustStorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // 1) 获取 quote
    public String fetchQuote() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/attestation/quote"))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("fetchQuote failed: " + resp.body());
        JsonNode node = mapper.readTree(resp.body());
        return node.get("quote").asText();
    }

    // 2) 提交 quote 到 verifier，获取 verification token（例如 internal verifier 返回 short-lived JWT）
    public String verifyQuoteWithVerifier(String base64Quote) throws Exception {
        String body = mapper.writeValueAsString(Map.of("quote", base64Quote));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(attestationVerifierUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("verifyQuote failed: " + resp.body());
        JsonNode node = mapper.readTree(resp.body());
        boolean verified = node.path("verified").asBoolean(false);
        if (!verified) throw new RuntimeException("quote not verified");
        // 假设 verifier 返回 short_lived_jwt
        return node.path("short_lived_jwt").asText();
    }

    // 3) 请求签名
    public JsonNode signPayload(String jwt, String keyId, byte[] payloadBytes) throws Exception {
        String payloadB64 = java.util.Base64.getEncoder().encodeToString(payloadBytes);
        String body = mapper.writeValueAsString(Map.of(
                "key_id", keyId,
                "payload", payloadB64,
                "purpose", "tx-sign"
        ));

        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/sign"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");

        if (jwt != null && !jwt.isEmpty()) {
            b.header("Authorization", "Bearer " + jwt);
        }

        HttpRequest req = b.build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("sign failed: " + resp.body());
        return mapper.readTree(resp.body());
    }

    // 4) 获取公钥
    public JsonNode fetchPubKey(String keyId) throws Exception {
        String url = baseUrl + "/pubkey" + (keyId != null ? ("?key_id=" + keyId) : "");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("fetchPubKey failed: " + resp.body());
        return mapper.readTree(resp.body());
    }

    // 5) 验证 ECDSA_secp256k1 签名 (签名为 base64)

    /**
     * 验证方法：与 main 调用一致
     * - message: 原始明文字节（签名时用的那一份）
     * - signatureB64: 签名（Base64）。如果 TEE 返回 raw (r||s) 64 字节，会自动转成 DER。
     * - pubkeyHex: 公钥 hex，可以是 "04..." 非压缩或 "02/03..." 压缩，也可带 0x 前缀。
     */
    public static boolean verifyEcdsaSecp256k1(byte[] message, String signatureB64, String pubkeyHex) throws Exception {
        // 1) normalize inputs
        if (pubkeyHex == null) throw new IllegalArgumentException("pubkeyHex is null");
        pubkeyHex = pubkeyHex.startsWith("0x") || pubkeyHex.startsWith("0X") ? pubkeyHex.substring(2) : pubkeyHex;

        // 2) decode signature (可能是 DER，也可能是 64-byte raw r||s)
        byte[] sigBytes = Base64.decode(signatureB64);
        if (sigBytes.length == 64) {
            // 转换 raw r||s -> ASN.1/DER （Java Signature 需要 DER 格式）
            sigBytes = rawConcatenatedRSBytesToDer(sigBytes);
        }

        // 3) 构造公钥（支持压缩/非压缩）
        PublicKey pubKey = loadSecp256k1PublicKeyFromHex(pubkeyHex);

        // 4) 验签（使用 SHA256withECDSA）
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC"); // 需已注册 BouncyCastle provider
        verifier.initVerify(pubKey);
        verifier.update(message);
        return verifier.verify(sigBytes);
    }

    /**
     * 把 64 字节 raw (r||s) 转成 ASN.1/DER
     */
    private static byte[] rawConcatenatedRSBytesToDer(byte[] raw) throws Exception {
        int len = raw.length / 2;
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(raw, 0, len));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(raw, len, raw.length));

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        return new DERSequence(v).getEncoded("DER");
    }

    /**
     * 从 hex 字符串解析 secp256k1 公钥（支持 compressed/uncompressed）并生成 Java PublicKey
     */
    private static PublicKey loadSecp256k1PublicKeyFromHex(String pubkeyHex) throws Exception {
        // 允许 pubkeyHex 带或不带 "04"/"02"/"03" 前缀
        // 使用 BouncyCastle 的 curve 参数并通过 ECNamedCurveSpec 转成 JCE 兼容的 ECParameterSpec
        ECNamedCurveParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        org.bouncycastle.jce.spec.ECNamedCurveSpec params =
                new org.bouncycastle.jce.spec.ECNamedCurveSpec(
                        bcSpec.getName(),
                        bcSpec.getCurve(),
                        bcSpec.getG(),
                        bcSpec.getN(),
                        bcSpec.getH(),
                        bcSpec.getSeed()
                );

        // decodePoint 能处理 compressed 或 uncompressed 的编码 (前缀 02/03/04)
        byte[] pubBytes = hexStringToByteArray(pubkeyHex);
        org.bouncycastle.math.ec.ECPoint q = bcSpec.getCurve().decodePoint(pubBytes);

        BigInteger x = q.getAffineXCoord().toBigInteger();
        BigInteger y = q.getAffineYCoord().toBigInteger();

        java.security.spec.ECPoint w = new java.security.spec.ECPoint(x, y);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(w, params);
        KeyFactory kf = KeyFactory.getInstance("EC", "BC");
        return kf.generatePublic(pubSpec);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            // 如果是奇数长度，前面补 0
            s = "0" + s;
            len = s.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    // 示例流程
    public static void main(String[] args) throws Exception {
        // 签名机服务地址
        String baseUrl = "https://signing-service.internal";
        // 远程证明（attestation）验证服务 的地址。
        String attestVerifier = "https://attest-verifier.internal/verify";

        //TEE服务端提供： 客户端自己的证书 + 私钥（通常由 PKI 签发）
        String keyStore = "/path/to/client-keystore.p12";
        String keyStorePwd = "changeit";
        //TEE服务端提供：用来信任签名机服务的根证书/中间证书。
        String trustStore = "/path/to/truststore.jks";
        String trustStorePwd = "changeit";
        TeeSigningClient client = new TeeSigningClient(baseUrl, attestVerifier, keyStore, keyStorePwd, trustStore, trustStorePwd);

        // 1. 获取 quote
        String quote = client.fetchQuote();
        System.out.println("quote(b64): " + quote.substring(0, Math.min(200, quote.length())) + "...");
        // 2. 提交 verifier 并获取短期 JWT
        String jwt = client.verifyQuoteWithVerifier(quote);
        System.out.println("got jwt: " + jwt);




        // 3. 请求签名
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);
        JsonNode signResp = client.signPayload(jwt, "sig-2025-01", payload);


        String signatureB64 = signResp.get("signature").asText();
        System.out.println("signature(b64): " + signatureB64);

        // 4. 获取公钥
        JsonNode pkResp = client.fetchPubKey("sig-2025-01");
        JsonNode keys = pkResp.get("keys");
        String pubkeyHex = keys.get(0).get("pubkey").asText();
        System.out.println("pubkey: " + pubkeyHex);

        // 5. 验证签名
//        boolean ok = client.verifyEcdsaSecp256k1(payload, signatureB64, pubkeyHex);
//        String payload = "HelloTEE";


        boolean ok = client.verifyEcdsaSecp256k1(payload, signatureB64, pubkeyHex);


        System.out.println("verify: " + ok);
    }
}
