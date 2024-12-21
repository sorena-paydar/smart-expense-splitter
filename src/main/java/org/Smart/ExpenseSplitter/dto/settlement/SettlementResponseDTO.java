package org.Smart.ExpenseSplitter.dto.settlement;

import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.SettlementEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponseDTO(
        Long id,
        GroupResponseDTO group,
        UserResponseDTO payer,
        UserResponseDTO payee,
        BigDecimal amountSettled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public SettlementResponseDTO(
            GroupResponseDTO group,
            UserResponseDTO payer,
            UserResponseDTO payee,
            BigDecimal amountSettled
    ) {
        this(null, group, payer, payee, amountSettled, null, null);
    }

    public SettlementResponseDTO(SettlementEntity settlementEntity) {
        this(
                null,
                new GroupResponseDTO(settlementEntity.getGroup()),
                new UserResponseDTO(settlementEntity.getPayer()),
                new UserResponseDTO(settlementEntity.getPayee()),
                settlementEntity.getAmount(),
                settlementEntity.getCreatedAt(),
                settlementEntity.getUpdatedAt()
        );
    }
}
