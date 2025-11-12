package com.finance.manager.controller;

import com.finance.manager.dto.budget.BudgetResponse;
import com.finance.manager.dto.budget.CreateBudgetRequest;
import com.finance.manager.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Budgets", description = "Budget management endpoints")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "List budgets for a period", description = "Retrieves budgets for a specific month and year")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<BudgetResponse> budgets = budgetService.getBudgetsByPeriod(month, year);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    @Operation(summary = "Create or update budget", description = "Creates a new budget or updates existing one for a category and period")
    public ResponseEntity<BudgetResponse> createOrUpdateBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse budget = budgetService.createOrUpdateBudget(request);
        return new ResponseEntity<>(budget, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Deletes a budget")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

}
