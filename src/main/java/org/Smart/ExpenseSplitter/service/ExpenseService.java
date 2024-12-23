package org.Smart.ExpenseSplitter.service;

import jakarta.transaction.Transactional;
import org.Smart.ExpenseSplitter.dto.expense.ExpenseRequestDTO;
import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.ExpenseNotFoundException;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.repository.ExpenseRepository;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.Smart.ExpenseSplitter.type.ExpenseType;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;
    private final AuthService userService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, GroupService groupService, AuthService userService, GroupRepository groupRepository, UserRepository userRepository, BalanceService balanceService) {
        this.expenseRepository = expenseRepository;
        this.groupService = groupService;
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.balanceService = balanceService;
    }

    @Transactional
    public ExpenseEntity addExpense(Long groupId, ExpenseRequestDTO expenseRequestDTO) throws BadRequestException {
        BigDecimal amount = expenseRequestDTO.getAmount();
        GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException("Group not found"));
        UserEntity payer = userRepository.findById(expenseRequestDTO.getPayerId()).orElseThrow(() -> new UserNotFoundException("Payer not found"));
        List<UserEntity> participants = userRepository.findAllById(expenseRequestDTO.getParticipantIds());

        BigDecimal splitAmount = amount.divide(BigDecimal.valueOf(participants.size()), RoundingMode.HALF_UP);

        ExpenseEntity expense = new ExpenseEntity();
        expense.setGroup(group);
        expense.setPayer(payer);
        expense.setAmount(amount);
        expense.setExpenseType(ExpenseType.valueOf(expenseRequestDTO.getExpenseType()));
        expense.setDescription(expenseRequestDTO.getDescription());
        expense.setParticipants(participants);

        ExpenseEntity savedExpense = expenseRepository.save(expense);

        // Update balances for each participant
        for (UserEntity participant : participants) {
            if (!participant.getId().equals(payer.getId())) {
                balanceService.updateBalance(groupId, participant.getId(), payer.getId(), splitAmount);
            }
        }

        return savedExpense;
    }

    /**
     * Updates an existing expense.
     * Only allows if the user is the owner of the expense or a member of the group.
     *
     * @param expenseId         The ID of the expense to update.
     * @param expenseRequestDTO The new data for the expense.
     * @return The updated expense entity.
     */
    @Transactional
    public ExpenseEntity updateExpense(Long expenseId, ExpenseRequestDTO expenseRequestDTO) throws AccessDeniedException {
        // Find the existing expense
        ExpenseEntity existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        // Update the expense entity
        existingExpense.setDescription(expenseRequestDTO.getDescription());
        existingExpense.setAmount(expenseRequestDTO.getAmount());
        existingExpense.setExpenseType(ExpenseType.valueOf(expenseRequestDTO.getExpenseType()));

        return expenseRepository.save(existingExpense);
    }

    /**
     * Deletes an expense.
     * Only allows if the user is the owner of the expense or a member of the group.
     *
     * @param expenseId The ID of the expense to delete.
     */
    @Transactional
    public void deleteExpense(Long expenseId) throws AccessDeniedException {
        // Find the expense to delete
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        // Delete the expense
        expenseRepository.delete(expense);
    }

    /**
     * Fetches expenses for a specific group.
     *
     * @param groupId  The ID of the group to fetch expenses for.
     * @param pageable Pagination information.
     * @return A paginated list of expenses for the specified group.
     */
    public Page<ExpenseEntity> getGroupExpenses(Long groupId, Pageable pageable) throws BadRequestException {
        if (!groupService.isCurrentUserMemberOfGroup(groupId) && !groupService.isCurrentUserGroupOwner(groupId)) {
            throw new BadRequestException("User is not joined to this group");
        }

        return expenseRepository.findByGroupId(groupId, pageable);
    }


    /**
     * Fetches user expenses.
     *
     * @param pageable Pagination information.
     * @return A paginated list of expenses for the specified user.
     */
    public Page<ExpenseEntity> getUserExpenses(Pageable pageable) throws AccessDeniedException {
        UserEntity currentUser = userService.getCurrentUser();
        return expenseRepository.findByPayerId(currentUser.getId(), pageable);
    }

    /**
     * Fetches the details of a specific expense.
     * Requires the user to be the owner of the expense or a member of the group.
     *
     * @param expenseId The ID of the expense to fetch.
     * @return The expense entity.
     */
    public ExpenseEntity getExpenseDetail(Long expenseId) throws AccessDeniedException {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
    }

    /**
     * Checks if the current authenticated user is a member of the group or the group owner associated with the given expenseId.
     *
     * @param expenseId The ID of the expense.
     * @return true if the user is a member or the owner of the group, false otherwise.
     */
    @Transactional
    public boolean isUserMemberOrOwnerOfGroupByExpense(Long expenseId) {
        // Fetch the expense
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Extract the group associated with the expense
        GroupEntity group = expense.getGroup();

        // Check if the user is a member of the group or the group owner
        return groupService.isCurrentUserMemberOfGroup(group.getId()) || groupService.isCurrentUserGroupOwner(group.getId());
    }

    /**
     * Checks if the current authenticated user is a member of the group associated with the given expenseId.
     *
     * @param expenseId The ID of the expense.
     * @return true if the user is a member of the group, false otherwise.
     */
    @Transactional
    public boolean isUserMemberOfGroupByExpense(Long expenseId) {
        // Fetch the expense
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Extract the group associated with the expense
        GroupEntity group = expense.getGroup();

        // Check if the user is a member of the group
        return groupService.isCurrentUserMemberOfGroup(group.getId());
    }


    /**
     * Checks if the current authenticated user is the group owner associated with the given expenseId.
     *
     * @param expenseId The ID of the expense.
     * @return true if the user is the owner of the group, false otherwise.
     */
    @Transactional
    public boolean isGroupOwnerByExpense(Long expenseId) {
        // Fetch the expense
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Extract the group associated with the expense
        GroupEntity group = expense.getGroup();

        // Check if the user is the group owner
        return groupService.isCurrentUserGroupOwner(group.getId());
    }

    /**
     * Checks if the current authenticated user is the creator of the specified expense.
     *
     * @param expenseId The ID of the expense to check.
     * @return true if the current user is the creator of the expense, false otherwise.
     */
    @Transactional
    public boolean isCurrentUserExpensePayer(Long expenseId) {
        UserEntity currentUser = userService.getCurrentUser();

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        return expense.getPayer().getId().equals(currentUser.getId());
    }
}
