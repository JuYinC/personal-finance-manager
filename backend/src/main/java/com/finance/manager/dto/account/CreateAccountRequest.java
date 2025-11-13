package com.finance.manager.dto.account;

import com.finance.manager.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 255, message = "Account name must be between 1 and 255 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    @Builder.Default
    private String currency = "TWD";

}
