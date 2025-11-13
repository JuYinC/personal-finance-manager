package com.finance.manager.repository;

import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query(value = "SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE a.user.id = :userId AND t.deletedAt IS NULL",
           countQuery = "SELECT COUNT(t) FROM Transaction t " +
           "JOIN t.account a " +
           "WHERE a.user.id = :userId AND t.deletedAt IS NULL")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE t.id = :id AND a.user.id = :userId AND t.deletedAt IS NULL")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(value = "SELECT t.* FROM transactions t " +
           "JOIN accounts a ON a.id = t.account_id " +
           "JOIN categories c ON c.id = t.category_id " +
           "WHERE a.user_id = CAST(:userId AS uuid) " +
           "AND (CAST(:accountId AS uuid) IS NULL OR t.account_id = CAST(:accountId AS uuid)) " +
           "AND (CAST(:categoryId AS uuid) IS NULL OR t.category_id = CAST(:categoryId AS uuid)) " +
           "AND (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar)) " +
           "AND (CAST(:startDate AS date) IS NULL OR t.transaction_date >= CAST(:startDate AS date)) " +
           "AND (CAST(:endDate AS date) IS NULL OR t.transaction_date <= CAST(:endDate AS date)) " +
           "AND t.deleted_at IS NULL",
           countQuery = "SELECT COUNT(*) FROM transactions t " +
           "JOIN accounts a ON a.id = t.account_id " +
           "WHERE a.user_id = CAST(:userId AS uuid) " +
           "AND (CAST(:accountId AS uuid) IS NULL OR t.account_id = CAST(:accountId AS uuid)) " +
           "AND (CAST(:categoryId AS uuid) IS NULL OR t.category_id = CAST(:categoryId AS uuid)) " +
           "AND (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar)) " +
           "AND (CAST(:startDate AS date) IS NULL OR t.transaction_date >= CAST(:startDate AS date)) " +
           "AND (CAST(:endDate AS date) IS NULL OR t.transaction_date <= CAST(:endDate AS date)) " +
           "AND t.deleted_at IS NULL",
           nativeQuery = true)
    Page<Transaction> findByFilters(@Param("userId") UUID userId,
                                    @Param("accountId") UUID accountId,
                                    @Param("categoryId") UUID categoryId,
                                    @Param("type") String type,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.account.id = :accountId AND t.deletedAt IS NULL")
    boolean existsActiveByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.category.id = :categoryId AND t.deletedAt IS NULL")
    boolean existsActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE a.user.id = :userId " +
           "AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.deletedAt IS NULL")
    List<Transaction> findByUserIdAndTypeAndDateRange(@Param("userId") UUID userId,
                                                      @Param("type") TransactionType type,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

}
