package com.example.digitalwalletsystem.repository;

import com.example.digitalwalletsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    @Query("""
        SELECT t FROM Transaction t
        WHERE t.fromAccount.id = :accountId
           OR t.toAccount.id = :accountId
        ORDER BY t.timestamp DESC
    """)
    List<Transaction> findHistoryByAccountId(@Param("accountId") Long accountId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.fromAccount.id = :accountId
          AND (t.type = 'EXPENSE' OR t.type = 'TRANSFER')
    """)
    BigDecimal calculateTotalSpentByAccountId(@Param("accountId") Long accountId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.toAccount.id = :accountId
          AND (t.type = 'INCOME' OR t.type = 'DEPOSIT' OR t.type = 'TRANSFER')
    """)
    BigDecimal calculateTotalIncomeByAccountId(@Param("accountId") Long accountId);

    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN t.fromAccount fa
        LEFT JOIN t.toAccount ta
        WHERE (
            :userId IS NULL 
            OR (fa IS NOT NULL AND fa.user.id = :userId) 
            OR (ta IS NOT NULL AND ta.user.id = :userId)
        )
        AND (
            :accountId IS NULL 
            OR (fa IS NOT NULL AND fa.id = :accountId) 
            OR (ta IS NOT NULL AND ta.id = :accountId)
        )
        AND (:type IS NULL OR t.type = :type)
        AND (:category IS NULL OR t.category = :category)
        AND (:fromDate IS NULL OR t.timestamp >= :fromDate)
        AND (:toDate IS NULL OR t.timestamp <= :toDate)
        ORDER BY t.timestamp DESC
    """)
    List<Transaction> findFilteredHistory(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("type") String type,
            @Param("category") String category,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}