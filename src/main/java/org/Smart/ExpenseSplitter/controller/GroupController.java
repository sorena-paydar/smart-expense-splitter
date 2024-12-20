package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.service.GroupService;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Get group detail")
    @GetMapping("/{groupId}")
    public ResponseEntity<JsonResponse> getGroupDetail(@PathVariable Long groupId) {
        try {
            GroupEntity group = groupService.getGroupDetail(groupId);
            return ResponseEntity.ok(new JsonResponse(true, "Group detail retrieved successfully", group));
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Get all groups owned or joined by the authenticated user")
    @GetMapping
    public ResponseEntity<JsonResponse> getUserGroups(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id,asc")
            Pageable pageable
    ) {
        try {
            Page<GroupEntity> userGroups = groupService.getUserGroups(pageable);
            return ResponseEntity.ok(new JsonResponse(true, "List of groups retrieved successfully", userGroups));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Create a new group")
    @PostMapping("/create")
    public ResponseEntity<JsonResponse> createGroup(@RequestParam String groupName) {
        try {
            GroupEntity group = groupService.createGroup(groupName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new JsonResponse(true, "Group created successfully", group));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Join an existing group")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<JsonResponse> joinGroup(@PathVariable Long groupId) {
        try {
            GroupEntity group = groupService.joinGroup(groupId);
            return ResponseEntity.ok(new JsonResponse(true, "Joined group successfully", group));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Leave a group")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<JsonResponse> leaveGroup(@PathVariable Long groupId) {
        try {
            GroupEntity group = groupService.leaveGroup(groupId);
            return ResponseEntity.ok(new JsonResponse(true, "Left group successfully", group));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    @Operation(summary = "Delete a group (only for the owner)")
    @DeleteMapping("/{groupId}/delete")
    public ResponseEntity<JsonResponse> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok(new JsonResponse(true, "Group deleted successfully", null));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }
}
