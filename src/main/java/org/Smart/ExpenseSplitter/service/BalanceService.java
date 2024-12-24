package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.dto.balance.BalanceResponseDTO;
import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.BalanceId;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.handler.Transaction;
import org.Smart.ExpenseSplitter.handler.UserBalance;
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
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuthService userService;
    private final GroupService groupService;

    public BalanceService(BalanceRepository balanceRepository, GroupRepository groupRepository,
                          UserRepository userRepository, AuthService userService, GroupService groupService) {
        this.balanceRepository = balanceRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
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

    // Fetch current user's balances as DTO
    public Page<BalanceResponseDTO> getUserBalancesAsDTO(Pageable pageable) {
        Page<BalanceEntity> userBalances = getBalancesForCurrentUser(pageable);

        List<BalanceResponseDTO> groupResponseDTOs = userBalances.getContent().stream()
                .map(BalanceResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(groupResponseDTOs, userBalances.getPageable(), userBalances.getTotalElements());
    }

    @Transactional
    public BalanceEntity settleBalance(Long groupId, Long fromUserId, Long toUserId, BigDecimal amount) throws BadRequestException {

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

    @Transactional
    public void optimizeDebts(Long groupId) {
        // Step 1: Fetch all balances for the group
        List<BalanceEntity> balances = balanceRepository.findByGroupId(groupId);
        if (balances.isEmpty()) return;

        // Step 2: Calculate net balances
        Map<Long, BigDecimal> netBalances = new HashMap<>();

        for (BalanceEntity balance : balances) {
            Long fromUser = balance.getId().getUserId();
            Long toUser = balance.getId().getOwesTo();
            BigDecimal amount = balance.getAmount();

            // Subtract amount from the payer's net balance
            netBalances.put(fromUser, netBalances.getOrDefault(fromUser, BigDecimal.ZERO).subtract(amount));

            // Add amount to the payee's net balance
            netBalances.put(toUser, netBalances.getOrDefault(toUser, BigDecimal.ZERO).add(amount));
        }


        // Step 3: Simplify debts using the net balances
        List<Transaction> transactions = simplifyDebts(netBalances);

        // Step 4: Update the database with optimized transactions
        balanceRepository.deleteByGroupId(groupId); // Clear existing balances

        for (Transaction transaction : transactions) {
            BalanceEntity balance = new BalanceEntity();
            BalanceId balanceId = new BalanceId(groupId, transaction.getFromUser(), transaction.getToUser());

            GroupEntity group = groupService.getGroupById(groupId);
            UserEntity fromUser = userService.getUserById(transaction.getFromUser());
            UserEntity toUser = userService.getUserById(transaction.getToUser());

            balance.setId(balanceId);
            balance.setGroup(group);
            balance.setUser(fromUser);
            balance.setOwesTo(toUser);
            balance.setAmount(transaction.getAmount());

            balanceRepository.save(balance);
        }
    }

    private List<Transaction> simplifyDebts(Map<Long, BigDecimal> netBalances) {
        List<Transaction> transactions = new ArrayList<>();
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>(Comparator.comparing(UserBalance::getBalance).reversed());
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(Comparator.comparing(UserBalance::getBalance));

        // Separate creditors and debtors
        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            Long userId = entry.getKey();
            BigDecimal balance = entry.getValue();

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(userId, balance));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalance(userId, balance.abs()));
            }
        }

        // Match debtors with creditors
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            UserBalance creditor = creditors.poll();
            UserBalance debtor = debtors.poll();

            assert debtor != null;
            BigDecimal amount = creditor.getBalance().min(debtor.getBalance());
            transactions.add(new Transaction(debtor.getUserId(), creditor.getUserId(), amount));

            if (creditor.getBalance().compareTo(amount) > 0) {
                creditors.add(new UserBalance(creditor.getUserId(), creditor.getBalance().subtract(amount)));
            }

            if (debtor.getBalance().compareTo(amount) > 0) {
                debtors.add(new UserBalance(debtor.getUserId(), debtor.getBalance().subtract(amount)));
            }
        }

        return transactions;
    }
}
