package com.web3;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BalanceChecker {
    private static final String API_URL = "https://blockstream.info/testnet/api/address/";

    public static void checkBalance(String address) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + address + "/utxo"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode utxos = mapper.readTree(response.body());

        long totalSatoshis = 0;
        for (JsonNode utxo : utxos) {
            totalSatoshis += utxo.get("value").asLong();
        }

        double balanceBTC = totalSatoshis / 1e8; // 1 BTC = 100,000,000 satoshis
        System.out.println("地址: " + address);
        System.out.println("余额: " + balanceBTC + " BTC (" + totalSatoshis + " satoshis)");
    }

    public static void main(String[] args) throws Exception {
        checkBalance("mnpKBukTnnwjTrYEPkdT34FsWkT5gb1Spr");
    }
}