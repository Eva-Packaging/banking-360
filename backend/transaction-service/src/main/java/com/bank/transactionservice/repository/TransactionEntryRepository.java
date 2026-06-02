package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.TransactionEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
}