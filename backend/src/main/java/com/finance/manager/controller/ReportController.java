package com.finance.manager.controller;

import com.finance.manager.dto.report.CategorySpendingResponse;
import com.finance.manager.dto.report.MonthlyTrendResponse;
import com.finance.manager.dto.report.SummaryResponse;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reports", description = "Financial reporting endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get income vs expense summary", description = "Retrieves income, expense, and net savings for a specific period")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        SummaryResponse summary = reportService.getSummary(month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get spending by category", description = "Retrieves spending breakdown by category for a date range")
    public ResponseEntity<List<CategorySpendingResponse>> getCategorySpending(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam TransactionType type) {
        List<CategorySpendingResponse> spending = reportService.getCategorySpending(startDate, endDate, type);
        return ResponseEntity.ok(spending);
    }

    @GetMapping("/trends")
    @Operation(summary = "Get monthly trends", description = "Retrieves monthly income and expense trends for the last N months")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") Integer months) {
        List<MonthlyTrendResponse> trends = reportService.getMonthlyTrends(months);
        return ResponseEntity.ok(trends);
    }

}
