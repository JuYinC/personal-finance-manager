package com.finance.manager.service;

import com.finance.manager.dto.report.CategorySpendingResponse;
import com.finance.manager.dto.report.MonthlyTrendResponse;
import com.finance.manager.dto.report.SummaryResponse;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public SummaryResponse getSummary(Integer month, Integer year) {
        UUID userId = userService.getCurrentUserId();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTypeAndDateRange(
                userId, null, startDate, endDate);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .month(month)
                .year(year)
                .build();
    }

    public List<CategorySpendingResponse> getCategorySpending(
            LocalDate startDate, LocalDate endDate, TransactionType type) {
        UUID userId = userService.getCurrentUserId();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTypeAndDateRange(
                userId, type, startDate, endDate);

        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<Transaction>> transactionsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));

        List<CategorySpendingResponse> categorySpending = new ArrayList<>();

        for (Map.Entry<UUID, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            List<Transaction> categoryTransactions = entry.getValue();
            Transaction firstTransaction = categoryTransactions.get(0);

            BigDecimal categoryTotal = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? categoryTotal.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            CategorySpendingResponse response = CategorySpendingResponse.builder()
                    .categoryId(firstTransaction.getCategory().getId())
                    .categoryName(firstTransaction.getCategory().getName())
                    .categoryIcon(firstTransaction.getCategory().getIcon())
                    .categoryColor(firstTransaction.getCategory().getColor())
                    .amount(categoryTotal)
                    .transactionCount((long) categoryTransactions.size())
                    .percentage(percentage)
                    .build();

            categorySpending.add(response);
        }

        categorySpending.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        return categorySpending;
    }

    public List<MonthlyTrendResponse> getMonthlyTrends(Integer months) {
        UUID userId = userService.getCurrentUserId();
        List<MonthlyTrendResponse> trends = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            List<Transaction> transactions = transactionRepository.findByUserIdAndTypeAndDateRange(
                    userId, null, startDate, endDate);

            BigDecimal income = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            MonthlyTrendResponse trend = MonthlyTrendResponse.builder()
                    .month(targetMonth.getMonthValue())
                    .year(targetMonth.getYear())
                    .income(income)
                    .expense(expense)
                    .netSavings(income.subtract(expense))
                    .build();

            trends.add(trend);
        }

        return trends;
    }

}
