package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Spring implements save(), findById(), etc. — no method body needed yet
}