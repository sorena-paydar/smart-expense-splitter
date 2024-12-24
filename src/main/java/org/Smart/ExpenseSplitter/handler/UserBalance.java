package org.Smart.ExpenseSplitter.handler;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class UserBalance {
    private Long userId;
    private BigDecimal balance;

    public UserBalance(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }
}