package com.bank.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: getters, setters, equals, hashCode, toString for all fields
@NoArgsConstructor // Spring/Jackson need empty constructor to build object from JSON
@AllArgsConstructor // handy for tests
public class TransferRequest {

    @NotNull // must be present in JSON (validation runs before service logic)
    private UUID fromAccountId;

    @NotNull
    private UUID toAccountId;

    @NotNull
    @Positive // ticket rule: amount > 0
    private BigDecimal amount;

    private String description; // optional — no @NotNull (API allows omit or empty)
}




//This class lives in the dto package (Data Transfer Object — API shapes).
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// ? Lombok writes getters/setters for you so the file stays short. Spring/Jackson need a no-arg constructor to build the object from JSON.

