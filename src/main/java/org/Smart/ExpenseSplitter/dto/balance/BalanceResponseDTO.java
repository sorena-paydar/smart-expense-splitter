package org.Smart.ExpenseSplitter.dto.balance;

import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.BalanceEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceResponseDTO(
        Long id,
        UserResponseDTO user,
        GroupResponseDTO group,
        BigDecimal balance,
        LocalDateTime updatedAt
) {
    public BalanceResponseDTO(
            UserResponseDTO user,
            GroupResponseDTO group,
            BigDecimal balance,
            LocalDateTime updatedAt
    ) {
        this(null, user, group, balance, updatedAt);
    }


    public BalanceResponseDTO(BalanceEntity balanceEntity) {
        this(
                null,
                new UserResponseDTO(balanceEntity.getUser()),
                new GroupResponseDTO(balanceEntity.getGroup()),
                balanceEntity.getBalance(),
                balanceEntity.getUpdatedAt()
        );
    }
}
