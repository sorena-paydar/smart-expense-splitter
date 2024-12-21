package org.Smart.ExpenseSplitter.dto.group;

import org.Smart.ExpenseSplitter.dto.balance.BalanceResponseDTO;
import org.Smart.ExpenseSplitter.dto.expense.ExpenseResponseDTO;
import org.Smart.ExpenseSplitter.dto.settlement.SettlementResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.GroupEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Record for responding with expense details.
 */
public record GroupResponseDTO(
        Long id,
        String name,
        UserResponseDTO creator,
        List<UserResponseDTO> users,
        List<ExpenseResponseDTO> expenses,
        List<SettlementResponseDTO> settlements,
        List<BalanceResponseDTO> balances
) {

    public GroupResponseDTO(
            String name,
            UserResponseDTO creator,
            List<UserResponseDTO> users,
            List<ExpenseResponseDTO> expenses,
            List<SettlementResponseDTO> settlements,
            List<BalanceResponseDTO> balances
    ) {
        this(null, name, creator, users, expenses, settlements, balances);
    }

    public GroupResponseDTO(GroupEntity group) {
        this(
                group.getId(),
                group.getName(),
                new UserResponseDTO(group.getCreator()),
                Optional.ofNullable(group.getUsers())
                        .map(u -> u.stream().map(UserResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                Optional.ofNullable(group.getExpenses())
                        .map(u -> u.stream().map(ExpenseResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                Optional.ofNullable(group.getSettlements())
                        .map(u -> u.stream().map(SettlementResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                Optional.ofNullable(group.getBalances())
                        .map(u -> u.stream().map(BalanceResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of())
        );
    }
}
