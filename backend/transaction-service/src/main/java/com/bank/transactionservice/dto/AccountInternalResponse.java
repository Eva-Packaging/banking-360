package com.bank.transactionservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInternalResponse {

    private UUID customerId;
    private String status;   // e.g. ACTIVE
    private BigDecimal balance;
}