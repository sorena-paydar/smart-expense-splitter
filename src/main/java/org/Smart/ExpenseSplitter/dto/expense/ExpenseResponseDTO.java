package org.Smart.ExpenseSplitter.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record for responding with expense details.
 */
public record ExpenseResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        String expenseType,
        String groupName,
        String userName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Simplified constructor for lightweight responses.
     *
     * @param id          Expense ID
     * @param description Description of the expense
     * @param amount      Amount of the expense
     */
    public ExpenseResponseDTO(Long id, String description, BigDecimal amount) {
        this(id, description, amount, null, null, null, null, null);
    }
}
