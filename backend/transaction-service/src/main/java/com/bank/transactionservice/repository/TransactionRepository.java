package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Basic paginated query — finds all transactions for a customer
    Page<Transaction> findByCustomerId(UUID customerId, Pageable pageable);

    // Filtered query — type and/or status can be null (optional filters)
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
            "AND (:type IS NULL OR t.transactionType = :type) " +
            "AND (:status IS NULL OR t.status = :status)")
    Page<Transaction> findByCustomerIdWithFilters(
            @Param("customerId") UUID customerId,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable);
}