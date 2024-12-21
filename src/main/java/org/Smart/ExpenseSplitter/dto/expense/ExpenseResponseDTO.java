package org.Smart.ExpenseSplitter.dto.expense;

import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.Smart.ExpenseSplitter.type.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record for responding with expense details.
 */
public record ExpenseResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        ExpenseType expenseType,
        GroupResponseDTO group,
        UserResponseDTO payer,
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
    public ExpenseResponseDTO(Long id, String description, ExpenseType expenseType, BigDecimal amount) {
        this(id, description, amount, expenseType, null, null, null, null);
    }

    public ExpenseResponseDTO(ExpenseEntity expenseEntity) {
        this(
                null,
                expenseEntity.getDescription(),
                expenseEntity.getAmount(),
                expenseEntity.getExpenseType(),
                new GroupResponseDTO(expenseEntity.getGroup()),
                new UserResponseDTO(expenseEntity.getPayer()),
                expenseEntity.getCreatedAt(),
                expenseEntity.getUpdatedAt()
        );
    }
}
