package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.SettlementEntity;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository interface for managing {@link SettlementEntity} objects.
 * Provides methods to perform database operations related to settlements.
 */
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    /**
     * Finds all settlements where the given user is either the payer or the payee.
     *
     * @param payer The user who made the payment.
     * @param payee The user who received the payment.
     * @return a {@link Page} of {@link SettlementEntity} containing the settlements
     *         that the user is associated with, either as the creator or a member
     */
    Page<SettlementEntity> findByPayerOrPayee(UserEntity payer, UserEntity payee, Pageable pageable);

    /**
     * Finds all settlements within a specified group where the given user is the payer.
     *
     * @param group The group in which the settlements were made.
     * @param payer The user who made the payments.
     * @return a {@link Page} of {@link SettlementEntity} containing the settlements
     *         within the specified group where the given user is the payer
     */
    Page<SettlementEntity> findByGroupAndPayer(GroupEntity group, UserEntity payer, Pageable pageable);

    Page<SettlementEntity> findByGroupAndPayee(GroupEntity group, UserEntity payee, Pageable pageable);
}
