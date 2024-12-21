package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link ExpenseEntity} entities.
 * Provides methods for querying expenses by associated group or user.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    /**
     * Finds all expenses associated with a specific group.
     *
     * @param groupId the ID of the group
     * @param pageable the pagination information
     * @return a {@link Page} of {@link ExpenseEntity} objects related to the specified group
     */
    Page<ExpenseEntity> findByGroupId(Long groupId, Pageable pageable);

    Page<ExpenseEntity> findByPayerId(Long payerId, Pageable pageable);
}
