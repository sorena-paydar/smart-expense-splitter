package org.Smart.ExpenseSplitter.dto.expense;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.Smart.ExpenseSplitter.type.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@JsonInclude(JsonInclude.Include.NON_NULL)
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
    public ExpenseResponseDTO(ExpenseEntity expenseEntity) {
        this(
                expenseEntity.getId(),
                expenseEntity.getDescription(),
                expenseEntity.getAmount(),
                expenseEntity.getExpenseType(),
                new GroupResponseDTO(
                        expenseEntity.getId(),
                        expenseEntity.getGroup().getName(),
                        new UserResponseDTO(expenseEntity.getGroup().getOwner()),
                        expenseEntity.getGroup().getMembers(),
                        expenseEntity.getGroup().getCreatedAt(),
                        expenseEntity.getGroup().getUpdatedAt()
                ),
                new UserResponseDTO(expenseEntity.getPayer()),
                expenseEntity.getCreatedAt(),
                expenseEntity.getUpdatedAt()
        );
    }
}
