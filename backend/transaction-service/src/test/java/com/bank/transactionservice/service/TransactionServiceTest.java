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

@ExtendWith(MockitoExtension.class) // JUnit 5 + Mockito: enable @Mock / @InjectMocks
class TransactionServiceTest {

    @Mock // fake database for transactions
    private TransactionRepository transactionRepository;

    @Mock // fake database for transaction entries
    private TransactionEntryRepository transactionEntryRepository;

    @Mock // fake HTTP calls to account-service
    private AccountServiceClient accountServiceClient;

    @InjectMocks // real TransactionService — Mockito injects the three @Mock fields above
    private TransactionService transactionService;

    @Test
    void rejectsSameAccountTransfer() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                accountId,           // from
                accountId,           // to — same as from (invalid)
                new BigDecimal("100.00"),
                "test"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(customerId, request)
        );

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
                        otherCustomerId,              // customerId — wrong owner (not customerId)
                        "ACTIVE",                   // status
                        new BigDecimal("500.00")    // balance
                ));
        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("50.00"),
                "test"
        );

        assertThrows(
                ForbiddenAccessException.class,
                () -> transactionService.transfer(customerId, request)
        );

        verify(transactionRepository, never()).save(any()); // failed early — no transaction saved
    }

    @Test
void rejectsTransferWhenInsufficientBalance() {
    UUID customerId = UUID.randomUUID();
    UUID fromAccountId = UUID.randomUUID();
    UUID toAccountId = UUID.randomUUID();

    when(accountServiceClient.getAccount(fromAccountId))
            .thenReturn(new AccountInternalResponse(
                    customerId,                 // correct owner
                    "ACTIVE",
                    new BigDecimal("30.00")     // only $30 available
            ));

    TransferRequest request = new TransferRequest(
            fromAccountId,
            toAccountId,
            new BigDecimal("50.00"),            // trying to send $50 — too much
            "test"
    );

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.transfer(customerId, request)
    );

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
                    customerId,
                    "CLOSED",                       // not ACTIVE — transfer must fail
                    new BigDecimal("500.00")
            ));

    TransferRequest request = new TransferRequest(
            fromAccountId,
            toAccountId,
            new BigDecimal("50.00"),
            "test"
    );

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.transfer(customerId, request)
    );

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
                Transaction t = invocation.getArgument(0); // object service built
                t.setId(UUID.randomUUID());                // fake DB assigning primary key
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
            fromAccountId,
            toAccountId,
            new BigDecimal("50.00"),
            "test"
    );

    assertThrows(
            ForbiddenAccessException.class,
            () -> transactionService.transfer(customerId, request)
    );

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
            fromAccountId,
            toAccountId,
            new BigDecimal("50.00"),
            "test"
    );

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.transfer(customerId, request)
    );

    assertEquals("Destination account is not active", ex.getMessage());
    verify(transactionRepository, never()).save(any());
}
}