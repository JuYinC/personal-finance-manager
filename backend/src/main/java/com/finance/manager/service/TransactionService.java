package com.finance.manager.service;

import com.finance.manager.dto.transaction.CreateTransactionRequest;
import com.finance.manager.dto.transaction.TransactionResponse;
import com.finance.manager.dto.transaction.UpdateTransactionRequest;
import com.finance.manager.entity.*;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.repository.AccountRepository;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public Page<TransactionResponse> getAllTransactions(
            UUID accountId, UUID categoryId, TransactionType type,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        String typeString = type != null ? type.name() : null;
        return transactionRepository.findByFilters(userId, accountId, categoryId, typeString, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public TransactionResponse getTransactionById(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UUID userId = userService.getCurrentUserId();

        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getAccountId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Transaction transaction = Transaction.builder()
                .account(account)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();

        transaction = transactionRepository.save(transaction);

        updateAccountBalance(account, request.getAmount(), request.getType(), true);

        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request) {
        UUID userId = userService.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        Account account = transaction.getAccount();
        BigDecimal oldAmount = transaction.getAmount();
        TransactionType oldType = transaction.getType();

        updateAccountBalance(account, oldAmount, oldType, false);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());

        transaction = transactionRepository.save(transaction);

        updateAccountBalance(account, request.getAmount(), request.getType(), true);

        return mapToResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        Account account = transaction.getAccount();
        updateAccountBalance(account, transaction.getAmount(), transaction.getType(), false);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, TransactionType type, boolean add) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance;

        if (type == TransactionType.INCOME) {
            newBalance = add ? currentBalance.add(amount) : currentBalance.subtract(amount);
        } else {
            newBalance = add ? currentBalance.subtract(amount) : currentBalance.add(amount);
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

}
