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

@Entity // tells JPA: this class maps to a database table
@Table(name = "transactions") // exact table name in Postgres
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id // primary key column
    @GeneratedValue(strategy = GenerationType.UUID) // DB gets a new UUID on insert
    private UUID id;

    @Column(nullable = false)
    private UUID customerId; // customer_id — who owns the transfer

    @Column(nullable = false, unique = true, length = 50)
    private String transactionReference; // unique reference number

    @Column(nullable = false, length = 30)
    private String transactionType; // e.g. TRANSFER

    @Column(nullable = false, length = 30)
    private String status; // PENDING, COMPLETED, FAILED

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description; // optional

    @Column(length = 255)
    private String failureReason; // set when status is FAILED

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}