package com.finance.manager.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private Integer month;
    private Integer year;
    private LocalDateTime createdAt;

}
