package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BalanceRepository extends JpaRepository<BalanceEntity, Long> {

    List<BalanceEntity> findByGroup(GroupEntity group);

    Page<BalanceEntity> findByGroup(GroupEntity group, Pageable pageable);

    Optional<BalanceEntity> findByUserAndGroup(UserEntity user, GroupEntity group);
}
