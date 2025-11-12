package com.finance.manager.repository;

import com.finance.manager.entity.Account;
import com.finance.manager.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    List<Account> findByUserIdAndType(UUID userId, AccountType type);

    boolean existsByIdAndUserId(UUID id, UUID userId);

}
