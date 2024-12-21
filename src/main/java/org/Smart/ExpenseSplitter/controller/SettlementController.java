package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.entity.SettlementEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.service.SettlementService;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/settlements")
@Validated
@Tag(name = "Settlements", description = "Endpoints for managing user settlements, including adding and deleting settlements.")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;

    }

    @GetMapping("/user")
    public ResponseEntity<JsonResponse> getUserSettlements(Pageable pageable) {
        try {
            Page<SettlementEntity> userSettlements = settlementService.getUserSettlements(pageable);

            return ResponseEntity.ok(new JsonResponse(true, "User settlements fetched successfully", userSettlements));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage(), e));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<JsonResponse> addSettlement(
            @RequestParam Long groupId,
            @RequestParam Long payeeId,
            @RequestParam BigDecimal amount
    ) throws BadRequestException {

        try {
            SettlementEntity addedSettlement = settlementService.addSettlement(groupId, payeeId, amount);
            return ResponseEntity.ok(new JsonResponse(true, "Settlement added", addedSettlement));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage(), e));
        }


    }

    @DeleteMapping("/undo/{settlementId}")
    public void undoSettlement(@PathVariable Long settlementId) throws BadRequestException {
        settlementService.undoSettlement(settlementId);
    }
}
