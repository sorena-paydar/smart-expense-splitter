package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.repository.BalanceRepository;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public BalanceService(BalanceRepository balanceRepository, GroupRepository groupRepository,
                          UserRepository userRepository) {
        this.balanceRepository = balanceRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }


    public Page<BalanceEntity> getBalancesForGroup(Long groupId, Pageable pageable) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        return balanceRepository.findByGroup(group, pageable);
    }

    @Transactional
    public void updateBalance(Long groupId, Long userId, BigDecimal amount) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<BalanceEntity> balanceEntityOptional = balanceRepository.findByUserAndGroup(user, group);

        BalanceEntity balance;

        if (balanceEntityOptional.isPresent()) {
            balance = balanceEntityOptional.get();
        } else {
            balance = new BalanceEntity();
            balance.setUser(user);
            balance.setGroup(group);
            balance.setBalance(BigDecimal.ZERO);
        }

        balance.setBalance(balance.getBalance().add(amount));
        balanceRepository.save(balance);
    }


    @Transactional
    public void resetBalancesForGroup(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        List<BalanceEntity> balances = balanceRepository.findByGroup(group);
        balances.forEach(balance -> balance.setBalance(BigDecimal.ZERO));
        balanceRepository.saveAll(balances);
    }
}
