package com.finance.manager.service;

import com.finance.manager.dto.budget.BudgetResponse;
import com.finance.manager.dto.budget.CreateBudgetRequest;
import com.finance.manager.entity.Budget;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<BudgetResponse> getBudgetsByPeriod(Integer month, Integer year) {
        UUID userId = userService.getCurrentUserId();
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse createOrUpdateBudget(CreateBudgetRequest request) {
        User user = userService.getCurrentUser();
        UUID userId = user.getId();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.getCategoryId(), request.getMonth(), request.getYear());

        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setAmount(request.getAmount());
        } else {
            budget = Budget.builder()
                    .user(user)
                    .category(category)
                    .amount(request.getAmount())
                    .month(request.getMonth())
                    .year(request.getYear())
                    .build();
        }

        budget = budgetRepository.save(budget);
        return mapToResponse(budget);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));

        budgetRepository.delete(budget);
    }

    private BudgetResponse mapToResponse(Budget budget) {
        BigDecimal spent = calculateSpentAmount(budget);
        BigDecimal remaining = budget.getAmount().subtract(spent);

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .amount(budget.getAmount())
                .spent(spent)
                .remaining(remaining.max(BigDecimal.ZERO))
                .month(budget.getMonth())
                .year(budget.getYear())
                .createdAt(budget.getCreatedAt())
                .build();
    }

    private BigDecimal calculateSpentAmount(Budget budget) {
        YearMonth yearMonth = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return transactionRepository.findByUserIdAndTypeAndDateRange(
                budget.getUser().getId(), TransactionType.EXPENSE, startDate, endDate)
                .stream()
                .filter(t -> t.getCategory().getId().equals(budget.getCategory().getId()))
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
