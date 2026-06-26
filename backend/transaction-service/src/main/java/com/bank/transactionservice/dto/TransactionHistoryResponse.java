package com.bank.transactionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    private UUID transactionId;       // maps from Transaction.id
    private UUID fromAccountId;       // null for now — not in Transaction entity directly
    private UUID toAccountId;         // null for now — not in Transaction entity directly
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String description;
    private Instant createdAt;
}