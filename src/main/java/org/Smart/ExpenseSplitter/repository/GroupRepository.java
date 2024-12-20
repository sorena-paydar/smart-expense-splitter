package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link GroupEntity} entities.
 * Provides methods for retrieving groups based on user relationships.
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    /**
     * Finds all groups that the given user has created or joined.
     *
     * @param user the user for whom to find groups
     * @param pageable the pagination information
     * @return a {@link Page} of {@link GroupEntity} containing the groups
     *         that the user is associated with, either as the creator or a member
     */
    @Query("SELECT g FROM GroupEntity g " +
            "WHERE g.creator = :user OR :user MEMBER OF g.users")
    Page<GroupEntity> findCurrentUserGroups(UserEntity user, Pageable pageable);
}
