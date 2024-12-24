package org.Smart.ExpenseSplitter.handler;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class Transaction {
    private Long fromUser;
    private Long toUser;
    private BigDecimal amount;

    public Transaction(Long fromUser, Long toUser, BigDecimal amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }
}