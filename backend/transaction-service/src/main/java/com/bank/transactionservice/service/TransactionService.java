package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountServiceClient;
import com.bank.transactionservice.dto.AccountInternalResponse;
import com.bank.transactionservice.dto.BalanceUpdateRequest;
import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.dto.TransferRequest;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.entity.TransactionEntry;
import com.bank.transactionservice.repository.TransactionEntryRepository;
import com.bank.transactionservice.repository.TransactionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.transactionservice.exception.ForbiddenAccessException;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AccountServiceClient accountServiceClient;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionEntryRepository transactionEntryRepository,
            AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.accountServiceClient = accountServiceClient;
    }

    @Transactional // all DB saves in this method succeed or roll back together
    public TransactionResponse transfer(UUID customerId, TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("From account and to account cannot be the same");
        }

        AccountInternalResponse fromAccount =
                accountServiceClient.getAccount(request.getFromAccountId());
        if (!fromAccount.getCustomerId().equals(customerId)) {
            throw new ForbiddenAccessException("Source account does not belong to customer");
        }
        if (!"ACTIVE".equals(fromAccount.getStatus())) {
            throw new IllegalArgumentException("Source account is not active");
        }
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }

        AccountInternalResponse toAccount =
                accountServiceClient.getAccount(request.getToAccountId());
        if (!toAccount.getCustomerId().equals(customerId)) {
           throw new ForbiddenAccessException("Destination account does not belong to customer");
        }
        if (!"ACTIVE".equals(toAccount.getStatus())) {
            throw new IllegalArgumentException("Destination account is not active");
        }

        Instant now = Instant.now();

        Transaction transaction = new Transaction();
        transaction.setCustomerId(customerId);
        transaction.setTransactionReference("TXN-" + UUID.randomUUID());
        transaction.setTransactionType("TRANSFER");
        transaction.setStatus("COMPLETED");
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEntry debitEntry = new TransactionEntry();
        debitEntry.setTransactionId(savedTransaction.getId());
        debitEntry.setAccountId(request.getFromAccountId());
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(request.getAmount());
        debitEntry.setBalanceAfter(fromAccount.getBalance().subtract(request.getAmount()));
        debitEntry.setCreatedAt(now);

        TransactionEntry creditEntry = new TransactionEntry();
        creditEntry.setTransactionId(savedTransaction.getId());
        creditEntry.setAccountId(request.getToAccountId());
        creditEntry.setEntryType("CREDIT");
        creditEntry.setAmount(request.getAmount());
        creditEntry.setBalanceAfter(toAccount.getBalance().add(request.getAmount()));
        creditEntry.setCreatedAt(now);

        transactionEntryRepository.save(debitEntry);
        transactionEntryRepository.save(creditEntry);

        accountServiceClient.updateBalance(
                request.getFromAccountId(),
                new BalanceUpdateRequest(request.getAmount().negate()));
        accountServiceClient.updateBalance(
                request.getToAccountId(),
                new BalanceUpdateRequest(request.getAmount()));

        return new TransactionResponse(
                savedTransaction.getId(),
                customerId,
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                "COMPLETED",
                "Transfer completed successfully",
                savedTransaction.getCreatedAt());
    }
}
