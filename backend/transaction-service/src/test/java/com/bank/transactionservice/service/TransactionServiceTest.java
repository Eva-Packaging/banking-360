package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountServiceClient;
import com.bank.transactionservice.repository.TransactionEntryRepository;
import com.bank.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.transactionservice.dto.TransferRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bank.transactionservice.dto.AccountInternalResponse;
import com.bank.transactionservice.exception.ForbiddenAccessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.entity.Transaction;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEntryRepository transactionEntryRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void rejectsSameAccountTransfer() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                accountId, accountId, new BigDecimal("100.00"), "test");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(customerId, request));

        assertEquals("From account and to account cannot be the same", ex.getMessage());
    }

    @Test
    void rejectsTransferWhenSourceAccountBelongsToAnotherCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        otherCustomerId, "ACTIVE", new BigDecimal("500.00")));

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, new BigDecimal("50.00"), "test");

        assertThrows(ForbiddenAccessException.class,
                () -> transactionService.transfer(customerId, request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsTransferWhenInsufficientBalance() {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "ACTIVE", new BigDecimal("30.00")));

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, new BigDecimal("50.00"), "test");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(customerId, request));

        assertEquals("Insufficient balance for transfer", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsTransferWhenSourceAccountIsInactive() {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "CLOSED", new BigDecimal("500.00")));

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, new BigDecimal("50.00"), "test");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(customerId, request));

        assertEquals("Source account is not active", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void completesTransferWhenAccountsAreValid() {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "ACTIVE", new BigDecimal("200.00")));
        when(accountServiceClient.getAccount(toAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "ACTIVE", new BigDecimal("100.00")));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, amount, "rent");

        TransactionResponse response = transactionService.transfer(customerId, request);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(amount, response.getAmount());
        assertEquals(fromAccountId, response.getFromAccountId());
        assertEquals(toAccountId, response.getToAccountId());

        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionEntryRepository, times(2)).save(any());
        verify(accountServiceClient).updateBalance(eq(fromAccountId), any());
        verify(accountServiceClient).updateBalance(eq(toAccountId), any());
    }

    @Test
    void rejectsTransferWhenDestinationAccountBelongsToAnotherCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "ACTIVE", new BigDecimal("200.00")));
        when(accountServiceClient.getAccount(toAccountId))
                .thenReturn(new AccountInternalResponse(
                        otherCustomerId, "ACTIVE", new BigDecimal("100.00")));

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, new BigDecimal("50.00"), "test");

        assertThrows(ForbiddenAccessException.class,
                () -> transactionService.transfer(customerId, request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsTransferWhenDestinationAccountIsInactive() {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountServiceClient.getAccount(fromAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "ACTIVE", new BigDecimal("200.00")));
        when(accountServiceClient.getAccount(toAccountId))
                .thenReturn(new AccountInternalResponse(
                        customerId, "FROZEN", new BigDecimal("100.00")));

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, new BigDecimal("50.00"), "test");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(customerId, request));

        assertEquals("Destination account is not active", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getMyTransactions_ReturnsPagedResponse_WhenTransactionsExist() {
        UUID customerId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setCustomerId(customerId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTransactionType("TRANSFER");
        transaction.setStatus("COMPLETED");
        transaction.setDescription("Test transfer");
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setTransactionReference("TXN-001");

        org.springframework.data.domain.Page<Transaction> page =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(transaction));

        when(transactionRepository.findByCustomerId(
                eq(customerId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        com.bank.transactionservice.dto.PagedTransactionResponse response =
                transactionService.getMyTransactions(customerId, 0, 10, null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COMPLETED", response.getContent().get(0).getStatus());
        assertEquals(new BigDecimal("100.00"), response.getContent().get(0).getAmount());
    }

    @Test
    void getMyTransactions_ReturnsEmptyPage_WhenNoTransactions() {
        UUID customerId = UUID.randomUUID();

        org.springframework.data.domain.Page<Transaction> emptyPage =
                org.springframework.data.domain.Page.empty();

        when(transactionRepository.findByCustomerId(
                eq(customerId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        com.bank.transactionservice.dto.PagedTransactionResponse response =
                transactionService.getMyTransactions(customerId, 0, 10, null, null);

        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void getMyTransactions_UsesFilteredQuery_WhenTypeProvided() {
        UUID customerId = UUID.randomUUID();

        org.springframework.data.domain.Page<Transaction> emptyPage =
                org.springframework.data.domain.Page.empty();

        when(transactionRepository.findByCustomerIdWithFilters(
                eq(customerId), eq("TRANSFER"), eq(null),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        transactionService.getMyTransactions(customerId, 0, 10, "TRANSFER", null);

        verify(transactionRepository).findByCustomerIdWithFilters(
                eq(customerId), eq("TRANSFER"), eq(null),
                any(org.springframework.data.domain.Pageable.class));
        verify(transactionRepository, never()).findByCustomerId(any(), any());
    }
}
