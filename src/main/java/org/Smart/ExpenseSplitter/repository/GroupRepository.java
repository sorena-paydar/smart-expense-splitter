package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link GroupEntity} entities.
 * Provides methods for retrieving groups based on user relationships.
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {


    GroupEntity findByName(String name);

    Page<GroupEntity> findByOwner(UserEntity user, Pageable pageable);
}
