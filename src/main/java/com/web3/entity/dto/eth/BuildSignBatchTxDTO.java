package com.web3.entity.dto.eth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量交易请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildSignBatchTxDTO {
    private List<BuildAndSignTransactionRequestDTO> txList = new ArrayList<>();
}
