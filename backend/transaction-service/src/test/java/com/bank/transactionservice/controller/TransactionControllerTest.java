package com.bank.transactionservice.controller;

import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.dto.TransferRequest;
import com.bank.transactionservice.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.transactionservice.exception.ForbiddenAccessException;
import com.bank.transactionservice.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

@Import(GlobalExceptionHandler.class) // load exception → JSON mapping for 400/403 tests
@AutoConfigureMockMvc(addFilters = false) // skip Spring Security filters in this slice test
@WebMvcTest(TransactionController.class) // load only TransactionController, not full app
class TransactionControllerTest {

    @MockitoBean // fake service — controller tests HTTP wiring, not business rules
    private TransactionService transactionService;

    @Autowired // Spring injects MockMvc for fake HTTP calls
    private MockMvc mockMvc;

    @Autowired // converts Java objects ↔ JSON in test requests/responses
    private ObjectMapper objectMapper;

    @Test
    void transfer_ShouldReturn201_WhenSuccessful() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");

        TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, amount, "rent");

        TransactionResponse response = new TransactionResponse(
                UUID.randomUUID(),
                customerId,
                fromAccountId,
                toAccountId,
                amount,
                "COMPLETED",
                "Transfer completed successfully",
                Instant.now());

        when(transactionService.transfer(any(UUID.class), any(TransferRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("X-User-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }
    @Test
    void transfer_ShouldReturn400_WhenSameAccount() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                accountId,
                accountId,
                new BigDecimal("50.00"),
                "test");

        when(transactionService.transfer(any(UUID.class), any(TransferRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "From account and to account cannot be the same"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("X-User-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("From account and to account cannot be the same"));
    }
    @Test
    void transfer_ShouldReturn403_WhenAccountNotOwnedByCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("50.00"),
                "test");

        when(transactionService.transfer(any(UUID.class), any(TransferRequest.class)))
                .thenThrow(new ForbiddenAccessException(
                        "Source account does not belong to customer"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("X-User-Id", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message")
                        .value("Source account does not belong to customer"));
    }
}
