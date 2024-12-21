package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.entity.SettlementEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.PayeeNotFoundException;
import org.Smart.ExpenseSplitter.exception.SettlementNotFoundException;
import org.Smart.ExpenseSplitter.repository.SettlementRepository;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service class for managing settlements.
 * This includes adding, undoing, and retrieving settlements for users and groups.
 */
@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuthService userService;
    private final BalanceService balanceService;

    public SettlementService(SettlementRepository settlementRepository, GroupRepository groupRepository, UserRepository userRepository, AuthService userService, BalanceService balanceService) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.balanceService = balanceService;
    }

    /**
     * Retrieves the settlements for the current authenticated user, either as a payer or payee.
     *
     * @param pageable The pageable object for pagination and sorting information.
     * @return A page of settlements where the current user is involved.
     */
    public Page<SettlementEntity> getUserSettlements(Pageable pageable) {
        UserEntity currentUser = userService.getCurrentUser();
        return settlementRepository.findByPayerOrPayee(currentUser, currentUser, pageable);
    }

    /**
     * Adds a new settlement for a group, specifying the payee and amount.
     *
     * @param groupId The ID of the group where the settlement occurred.
     * @param payeeId The ID of the user who received the payment.
     * @param amount  The amount of money settled.
     * @return The newly created settlement entity.
     * @throws BadRequestException If the current user attempts to settle with themselves or invalid group/payee IDs.
     */
    @Transactional
    public SettlementEntity addSettlement(Long groupId, Long payeeId, BigDecimal amount) throws BadRequestException {
        UserEntity currentUser = userService.getCurrentUser();
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        UserEntity payee = userRepository.findById(payeeId)
                .orElseThrow(() -> new PayeeNotFoundException("Payee not found"));

        if (payee.equals(currentUser)) {
            throw new BadRequestException("You cannot settle with yourself.");
        }

        SettlementEntity settlement = new SettlementEntity();
        settlement.setGroup(group);
        settlement.setPayer(currentUser);
        settlement.setPayee(payee);
        settlement.setAmount(amount);

        // Update balances after the settlement
        balanceService.updateBalance(groupId, currentUser.getId(), amount.negate());  // Decrease payer's balance
        balanceService.updateBalance(groupId, payeeId, amount);  // Increase payee's balance

        return settlementRepository.save(settlement);
    }

    /**
     * Deletes a settlement.
     *
     * @param settlementId The ID of the settlement to delete.
     * @throws BadRequestException If the current user is not involved in the settlement as a payer or payee.
     */
    @Transactional
    public void undoSettlement(Long settlementId) throws BadRequestException {
        SettlementEntity settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found"));

        UserEntity currentUser = userService.getCurrentUser();

        if (!settlement.getPayer().equals(currentUser) && !settlement.getPayee().equals(currentUser)) {
            throw new BadRequestException("You cannot undo this settlement");
        }

        settlementRepository.delete(settlement);
    }
}
