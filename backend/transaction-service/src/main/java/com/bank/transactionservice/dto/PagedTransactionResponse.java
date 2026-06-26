package com.bank.transactionservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedTransactionResponse {

    private List<TransactionHistoryResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}