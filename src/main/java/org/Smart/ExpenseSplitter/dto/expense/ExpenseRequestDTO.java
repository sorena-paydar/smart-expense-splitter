package org.Smart.ExpenseSplitter.dto.expense;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating or updating an expense.
 */
@Data
public class ExpenseRequestDTO {

    @NotNull
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String expenseType;
}
