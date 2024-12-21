package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.dto.expense.ExpenseRequestDTO;
import org.Smart.ExpenseSplitter.dto.expense.ExpenseResponseDTO;
import org.Smart.ExpenseSplitter.entity.ExpenseEntity;
import org.Smart.ExpenseSplitter.exception.ExpenseNotFoundException;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.service.ExpenseMapper;
import org.Smart.ExpenseSplitter.service.ExpenseService;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/expenses")
@Validated
@Tag(name = "Expenses", description = "Endpoints for managing user expenses, including adding, updating, and deleting expenses.")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    public ExpenseController(ExpenseService expenseService, ExpenseMapper expenseMapper) {
        this.expenseService = expenseService;
        this.expenseMapper = expenseMapper;
    }

    /**
     * Endpoint to get a list of expenses for the authenticated user.
     * Requires the user to be the one requesting their own expenses.
     */
    @Operation(summary = "Get a list of expenses for the authenticated user")
    @GetMapping
    public ResponseEntity<JsonResponse> getUserExpenses(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id,asc")
            Pageable pageable
    ) {
        try {
            // Fetch user expenses
            Page<ExpenseEntity> expenses = expenseService.getUserExpenses(pageable);

            // Map ExpenseEntities to ExpenseResponseDTOs
            Page<ExpenseResponseDTO> expenseResponseDTOs = expenses.map(expenseMapper::toResponseDTO);
            return ResponseEntity.ok(new JsonResponse(true, "Fetched expenses successfully", expenseResponseDTOs));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint to get the details of a specific expense.
     * Requires the user to be the owner or a member of the group to view the expense details.
     */
    @Operation(summary = "Get the details of a specific expense")
    @PreAuthorize("@expenseService.isUserMemberOrOwnerOfGroupByExpense(#expenseId)")
    @GetMapping("/{expenseId}")
    public ResponseEntity<JsonResponse> getExpenseDetail(@PathVariable Long expenseId) {
        try {
            ExpenseEntity expense = expenseService.getExpenseDetail(expenseId);
            ExpenseResponseDTO expenseResponseDTO = expenseMapper.toResponseDTO(expense);
            return ResponseEntity.ok(new JsonResponse(true, "Fetched expense detail successfully", expenseResponseDTO));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponse(false, e.getMessage(), null));
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JsonResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint to create a new expense within a group.
     * The user must be a member of the group to create the expense.
     *
     * @param groupId           The ID of the group where the expense will be created.
     * @param expenseRequestDTO The data for the expense to be created.
     * @return A response containing the created expense data.
     */
    @Operation(summary = "Create a new expense within a specific group")
    @PreAuthorize("@groupService.isUserMemberOfGroup(#groupId) OR @groupService.isGroupOwner(#groupId)")
    @PostMapping("/group/{groupId}/create")
    public ResponseEntity<JsonResponse> createExpense(
            @PathVariable Long groupId,
            @RequestBody ExpenseRequestDTO expenseRequestDTO
    ) {
        try {
            // Call the service to create the expense
            ExpenseEntity createdExpense = expenseService.addExpense(groupId, expenseRequestDTO);

            // Map the created expense to a response DTO
            ExpenseResponseDTO expenseResponseDTO = expenseMapper.toResponseDTO(createdExpense);

            return ResponseEntity.status(HttpStatus.CREATED).body(new JsonResponse(true, "Expense created successfully", expenseResponseDTO));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JsonResponse(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Endpoint to get a list of expenses for a specific group.
     * Requires the user to be a member of the group to view the expenses.
     */
    @PreAuthorize("@groupService.isUserMemberOfGroup(#groupId) OR @groupService.isGroupOwner(#groupId)")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<JsonResponse> getExpensesByGroupId(@PathVariable Long groupId, @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id,asc") org.springframework.data.domain.Pageable pageable) {
        try {
            Page<ExpenseEntity> expenses = expenseService.getExpensesByGroup(groupId, pageable);
            Page<ExpenseResponseDTO> expenseResponseDTOs = expenses.map(expenseMapper::toResponseDTO);
            return ResponseEntity.ok(new JsonResponse(true, "Fetched expenses successfully", expenseResponseDTOs));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint to update an expense.
     * Requires the user to be the creator of the expense to update the expense.
     */
    @Operation(summary = "Update an expense")
    @PreAuthorize("@expenseService.isCurrentUserCreatorOfExpense(#expenseId)")
    @PutMapping("/{expenseId}/update")
    public ResponseEntity<JsonResponse> updateExpense(
            @PathVariable Long expenseId,
            @RequestBody ExpenseRequestDTO expenseRequestDTO
    ) {
        try {
            ExpenseEntity updatedExpense = expenseService.updateExpense(expenseId, expenseRequestDTO);
            ExpenseResponseDTO expenseResponseDTO = expenseMapper.toResponseDTO(updatedExpense);
            return ResponseEntity.ok(new JsonResponse(true, "Updated expense successfully", expenseResponseDTO));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponse(false, e.getMessage(), null));
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JsonResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    /**
     * Endpoint to delete an expense.
     * Requires the user to be the creator of the expense to delete the expense.
     */
    @Operation(summary = "Delete an expense")
    @PreAuthorize("@expenseService.isCurrentUserCreatorOfExpense(#expenseId)")
    @DeleteMapping("/{expenseId}/delete")
    public ResponseEntity<JsonResponse> deleteExpense(@PathVariable Long expenseId) {
        try {
            expenseService.deleteExpense(expenseId);
            return ResponseEntity.ok(new JsonResponse(true, "Deleted expense successfully", null));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JsonResponse(false, e.getMessage(), null));
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JsonResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }
}
