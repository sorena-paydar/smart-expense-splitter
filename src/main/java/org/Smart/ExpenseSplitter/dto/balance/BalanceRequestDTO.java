package org.Smart.ExpenseSplitter.dto.balance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceRequestDTO {

    @NotNull
    private Long groupId;

    @NotNull
    private Long toUserId;

    @NotNull
    private BigDecimal amount;
}
