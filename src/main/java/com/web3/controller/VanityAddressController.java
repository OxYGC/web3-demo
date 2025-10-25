package com.web3.controller;

import com.web3.dto.ApiResponse;
import com.web3.dto.VanityAddressRequest;
import com.web3.dto.VanityAddressResult;
import com.web3.service.VanityAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/vanity")
@RequiredArgsConstructor
public class VanityAddressController {

    private final VanityAddressService vanityAddressService;

    /**
     * 生成靓号地址
     */
    @PostMapping("/generate")
    public ApiResponse<List<VanityAddressResult>> generateVanityAddresses(@Valid @RequestBody VanityAddressRequest request) {
        try {
            List<VanityAddressResult> results = vanityAddressService.generateVanityAddresses(
                request.getPattern(),
                request.getCoinType(),
                request.getMaxResults()
            );

            if (results.isEmpty()) {
                return ApiResponse.error("未找到符合条件的靓号地址，请尝试修改规则");
            }

            return ApiResponse.success("靓号地址生成成功", results);
        } catch (Exception e) {
            return ApiResponse.error("生成靓号地址失败: " + e.getMessage());
        }
    }

    /**
     * 下载靓号地址文件
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadVanityAddresses(@RequestBody List<VanityAddressResult> results) {
        try {
            String content = vanityAddressService.generateDownloadContent(results);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            String filename = "vanity_addresses_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                            ".txt";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
