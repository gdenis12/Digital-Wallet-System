package com.example.digitalwalletsystem.repository;

import com.example.digitalwalletsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.fromAccount.id = :accountId
           OR t.toAccount.id = :accountId
        ORDER BY t.timestamp DESC
    """)
    List<Transaction> findHistoryByAccountId(Long accountId);
}