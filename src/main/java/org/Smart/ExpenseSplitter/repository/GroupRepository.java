package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT g FROM GroupEntity g " +
            "WHERE g.creator = :user OR :user MEMBER OF g.users")
    Page<GroupEntity> findCurrentUserGroups(UserEntity user, Pageable pageable);
}
