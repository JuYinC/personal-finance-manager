package com.finance.manager.service;

import com.finance.manager.dto.account.AccountResponse;
import com.finance.manager.dto.account.CreateAccountRequest;
import com.finance.manager.dto.account.UpdateAccountRequest;
import com.finance.manager.entity.Account;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.AccountRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<AccountResponse> getAllAccounts() {
        UUID userId = userService.getCurrentUserId();
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        User user = userService.getCurrentUser();

        Account account = Account.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .build();

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        UUID userId = userService.getCurrentUserId();
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        account.setName(request.getName());
        account.setType(request.getType());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (transactionRepository.existsActiveByAccountId(id)) {
            throw new ValidationException("Cannot delete account with existing transactions");
        }

        accountRepository.delete(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

}
