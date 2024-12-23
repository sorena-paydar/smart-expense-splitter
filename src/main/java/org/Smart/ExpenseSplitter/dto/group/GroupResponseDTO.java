package org.Smart.ExpenseSplitter.dto.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.Smart.ExpenseSplitter.dto.balance.BalanceResponseDTO;
import org.Smart.ExpenseSplitter.dto.expense.ExpenseResponseDTO;
import org.Smart.ExpenseSplitter.dto.user.UserResponseDTO;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupResponseDTO(
        Long id,
        String name,
        UserResponseDTO creator,
        List<UserResponseDTO> users,
        List<ExpenseResponseDTO> expenses,
        List<BalanceResponseDTO> balances,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public GroupResponseDTO(
            Long id,
            String name,
            UserResponseDTO creator,
            List<UserEntity> users,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(
                id,
                name,
                creator,
                Optional.ofNullable(users)
                        .map(u -> u.stream().map(UserResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of())
                ,
                null,
                null,
                createdAt,
                updatedAt
        );
    }

    public GroupResponseDTO(GroupEntity group) {
        this(
                group.getId(),
                group.getName(),
                new UserResponseDTO(group.getOwner()),
                Optional.ofNullable(group.getMembers())
                        .map(u -> u.stream().map(UserResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                Optional.ofNullable(group.getExpenses())
                        .map(u -> u.stream().map(ExpenseResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                Optional.ofNullable(group.getBalances())
                        .map(u -> u.stream().map(BalanceResponseDTO::new).collect(Collectors.toList()))
                        .orElse(List.of()),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
