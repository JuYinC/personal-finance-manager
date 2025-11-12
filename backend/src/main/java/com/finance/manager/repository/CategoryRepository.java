package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user.id = :userId")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    List<Category> findByIsSystemTrue();

    List<Category> findByUserId(UUID userId);

    List<Category> findByUserIdAndType(UUID userId, TransactionType type);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

}
