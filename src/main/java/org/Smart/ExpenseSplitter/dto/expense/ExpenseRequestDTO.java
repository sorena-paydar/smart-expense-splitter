package org.Smart.ExpenseSplitter.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.Smart.ExpenseSplitter.type.ExpenseType;

import java.math.BigDecimal;
import java.util.List;


@Data
public class ExpenseRequestDTO {

    @Schema(description = "Expense's description", example = "PIZZA")
    @NotNull
    private String description;

    @Schema(description = "Expense's amount", example = "120")
    @NotNull
    private BigDecimal amount;

    @Schema(description = "Expense's type", example = "FOOD")
    @NotNull
    private String expenseType;

    private Long payerId;

    @NotNull
    private List<Long> participantIds;
}
