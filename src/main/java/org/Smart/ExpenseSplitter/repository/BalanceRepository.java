package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.BalanceId;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BalanceRepository extends JpaRepository<BalanceEntity, Long> {

    Optional<BalanceEntity> findById(BalanceId balanceId);

    Page<BalanceEntity> findByGroupId(Long groupId, Pageable pageable);

    List<BalanceEntity> findByGroupId(Long groupId);

    Page<BalanceEntity> findByUserId(Long userId, Pageable pageable);

    Page<BalanceEntity> findByOwesTo(UserEntity owesTo, Pageable pageable);

}
