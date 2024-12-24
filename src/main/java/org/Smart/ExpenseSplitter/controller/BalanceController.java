package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.dto.balance.BalanceRequestDTO;
import org.Smart.ExpenseSplitter.dto.balance.BalanceResponseDTO;
import org.Smart.ExpenseSplitter.entity.BalanceEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.service.AuthService;
import org.Smart.ExpenseSplitter.service.BalanceService;
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

@RestController
@RequestMapping("/balances")
@Validated
@Tag(name = "Balances", description = "Endpoints for managing balances.")
public class BalanceController {

    private final BalanceService balanceService;
    private final AuthService userService;

    public BalanceController(BalanceService balanceService, AuthService userService) {
        this.balanceService = balanceService;
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<JsonResponse> getUserBalances(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id,asc")
            Pageable pageable
    ) {
        try {
            Page<BalanceResponseDTO> userBalancesAsDTO = balanceService.getUserBalancesAsDTO(pageable);

            return ResponseEntity.ok(new JsonResponse(true, "User balances fetched successfully", userBalancesAsDTO));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage(), e));
        }
    }

    @PostMapping("/settle-up")
    @PreAuthorize("@groupService.isCurrentUserMemberOrOwnerOfGroup(#balanceRequestDTO.groupId)")
    public ResponseEntity<JsonResponse> settleBalance(BalanceRequestDTO balanceRequestDTO) {
        try {
            UserEntity currentUser = userService.getCurrentUser();
            Long fromUserId = currentUser.getId();

            BalanceEntity settledBalance = balanceService.settleBalance(
                    balanceRequestDTO.getGroupId(),
                    fromUserId,
                    balanceRequestDTO.getToUserId(),
                    balanceRequestDTO.getAmount()
            );

            BalanceResponseDTO balanceResponseDTO = new BalanceResponseDTO(settledBalance);
            return ResponseEntity.ok(new JsonResponse(true, "User balances fetched successfully", balanceResponseDTO));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage(), e));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage(), e));
        }
    }
}
