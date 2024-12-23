package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.dto.balance.BalanceResponseDTO;
import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.BalanceId;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.repository.BalanceRepository;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuthService userService;

    public BalanceService(BalanceRepository balanceRepository, GroupRepository groupRepository,
                          UserRepository userRepository, AuthService userService) {
        this.balanceRepository = balanceRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Fetch balances for a group with pagination
    public Page<BalanceEntity> getBalancesForGroup(Long groupId, Pageable pageable) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        return balanceRepository.findByGroupId(group.getId(), pageable);
    }

    // Fetch balances for the current user with pagination
    public Page<BalanceEntity> getBalancesForCurrentUser(Pageable pageable) {
        UserEntity currentUser = userService.getCurrentUser();
        return balanceRepository.findByUserId(currentUser.getId(), pageable);
    }

    // Update the balance (either create or update the existing balance between users)
    @Transactional
    public void updateBalance(Long groupId, Long fromUserId, Long toUserId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        BalanceId balanceId = new BalanceId(groupId, fromUserId, toUserId);
        Optional<BalanceEntity> optionalBalance = balanceRepository.findById(balanceId);

        BalanceEntity balance;
        if (optionalBalance.isPresent()) {
            balance = optionalBalance.get();
            balance.setAmount(balance.getAmount().add(amount));
        } else {
            GroupEntity group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found"));

            UserEntity fromUser = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new UserNotFoundException("From user not found"));

            UserEntity toUser = userRepository.findById(toUserId)
                    .orElseThrow(() -> new UserNotFoundException("To user not found"));

            balance = new BalanceEntity();
            balance.setId(balanceId);
            balance.setGroup(group);
            balance.setUser(fromUser);
            balance.setOwesTo(toUser);
            balance.setAmount(amount);
        }

        balanceRepository.save(balance);
    }

    // Reset all balances to zero for a specific group
    @Transactional
    public void resetBalancesForGroup(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        List<BalanceEntity> balances = balanceRepository.findByGroupId(group.getId());
        balances.forEach(balance -> balance.setAmount(BigDecimal.ZERO));
        balanceRepository.saveAll(balances);
    }

    // Fetch current user's balances as DTO
    public Page<BalanceResponseDTO> getUserBalancesAsDTO(Pageable pageable) {
        Page<BalanceEntity> userBalances = getBalancesForCurrentUser(pageable);

        List<BalanceResponseDTO> groupResponseDTOs = userBalances.getContent().stream()
                .map(BalanceResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(groupResponseDTOs, userBalances.getPageable(), userBalances.getTotalElements());
    }

    @Transactional
    public BalanceEntity settleBalance(Long groupId, Long toUserId, BigDecimal amount) throws BadRequestException {
        UserEntity currentUser = userService.getCurrentUser();
        Long fromUserId = currentUser.getId();

        BalanceId balanceId = new BalanceId(groupId, fromUserId, toUserId);
        Optional<BalanceEntity> optionalBalance = balanceRepository.findById(balanceId);

        if (optionalBalance.isEmpty()) {
            throw new BadRequestException("No outstanding balance found with the specified user in this group.");
        }

        BalanceEntity balance = optionalBalance.get();
        BigDecimal currentAmount = balance.getAmount();

        if (amount.compareTo(currentAmount) > 0) {
            throw new BadRequestException("The settlement amount exceeds the owed amount.");
        }

        // Update or delete the balance
        BigDecimal updatedAmount = currentAmount.subtract(amount);
        if (updatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            balanceRepository.delete(balance);
        } else {
            balance.setAmount(updatedAmount);
            balanceRepository.save(balance);
        }

        return balance;
    }
}
