package com.bank.transactionservice.client;

import com.bank.transactionservice.dto.AccountInternalResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bank.transactionservice.dto.BalanceUpdateRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountServiceClient {

    @GetMapping("/internal/accounts/{accountId}")
    AccountInternalResponse getAccount(@PathVariable("accountId") UUID accountId);
    @PatchMapping("/internal/accounts/{accountId}/balance")
    void updateBalance(
        @PathVariable("accountId") UUID accountId,
        @RequestBody BalanceUpdateRequest request);
}