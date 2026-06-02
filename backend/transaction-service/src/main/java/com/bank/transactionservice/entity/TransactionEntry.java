package com.bank.transactionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId; // FK to transactions.id

    @Column(nullable = false)
    private UUID accountId; // which account this line affects

    @Column(nullable = false, length = 20)
    private String entryType; // DEBIT or CREDIT

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2) // optional in DB
    private BigDecimal balanceAfter;

    @Column(nullable = false)
    private Instant createdAt;
}