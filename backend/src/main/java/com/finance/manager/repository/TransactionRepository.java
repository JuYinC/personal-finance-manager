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

    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE a.user.id = :userId AND t.deletedAt IS NULL")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE t.id = :id AND a.user.id = :userId AND t.deletedAt IS NULL")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.account a " +
           "JOIN FETCH t.category c " +
           "WHERE a.user.id = :userId " +
           "AND (:accountId IS NULL OR a.id = :accountId) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND t.deletedAt IS NULL")
    Page<Transaction> findByFilters(@Param("userId") UUID userId,
                                    @Param("accountId") UUID accountId,
                                    @Param("categoryId") UUID categoryId,
                                    @Param("type") TransactionType type,
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
