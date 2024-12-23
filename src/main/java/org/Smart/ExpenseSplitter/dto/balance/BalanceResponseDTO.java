package org.Smart.ExpenseSplitter.dto.balance;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.BalanceId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BalanceResponseDTO(
        BalanceId groupId,
        UserResponseDTO fromUser,   // User who owes money
        UserResponseDTO toUser,     // User who is owed money
        BigDecimal amount,          // Amount owed
        LocalDateTime updatedAt
) {
    public BalanceResponseDTO(BalanceEntity balanceEntity) {
        this(
                balanceEntity.getId(),
                new UserResponseDTO(balanceEntity.getUser()),
                new UserResponseDTO(balanceEntity.getOwesTo()),
                balanceEntity.getAmount(),
                balanceEntity.getUpdatedAt()
        );
    }
}
