package org.example.repository;

import org.example.model.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    boolean existsByAccountId(Long accountId);

    @Query("""
    SELECT transaction
    FROM AccountTransaction transaction
    WHERE (CAST(:from AS date) IS NULL OR transaction.transactionDate >= :from)
    AND (CAST(:to AS date) IS NULL OR transaction.transactionDate <= :to)
    AND (:category IS NULL OR transaction.category = :category)
    """)
    List<AccountTransaction> findTransactions(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category
    );
}
