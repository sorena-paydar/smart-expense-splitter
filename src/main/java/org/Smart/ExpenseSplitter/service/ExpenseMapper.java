package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.dto.expense.ExpenseResponseDTO;
import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDTO toResponseDTO(ExpenseEntity expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseType().toString(),
                expense.getGroup().getName(),
                expense.getUser().getUsername(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
