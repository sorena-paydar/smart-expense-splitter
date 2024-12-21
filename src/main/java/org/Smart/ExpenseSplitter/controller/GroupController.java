package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.dto.group.GroupRequestDTO;
import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user groups.
 * Provides endpoints to create, join, leave, and delete groups, as well as view details of specific groups
 * and get a list of all groups owned or joined by the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/groups")
@Validated
@Tag(name = "Groups", description = "Endpoints for managing user groups including creation, joining, leaving, and viewing groups.")
public class GroupController {

    private final GroupService groupService;

    /**
     * Constructor to initialize the GroupController with the GroupService dependency.
     *
     * @param groupService The service responsible for handling group-related operations.
     */
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Endpoint to fetch the details of a specific group.
     * Requires the group ID to fetch the details of the group.
     *
     * @param groupId The ID of the group whose details are to be retrieved.
     * @return A ResponseEntity containing the group details or an error message if not found.
     */
    @Operation(summary = "Get group detail")
    @GetMapping("/{groupId}")
    public ResponseEntity<JsonResponse> getGroupDetail(@PathVariable Long groupId) {
        try {
            GroupEntity groupDetail = groupService.getGroupDetail(groupId);
            GroupResponseDTO groupResponseDTO = new GroupResponseDTO(groupDetail);
            return ResponseEntity.ok(new JsonResponse(true, "Group detail fetched successfully", groupResponseDTO));
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to fetch all groups that the authenticated user is part of.
     * Includes groups that the user owns or has joined.
     *
     * @param pageable Pagination parameters for retrieving groups.
     * @return A paginated list of groups that the authenticated user is part of.
     */
    @Operation(summary = "Get all groups owned or joined by the authenticated user")
    @GetMapping
    public ResponseEntity<JsonResponse> getUserGroups(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id,asc")
            Pageable pageable
    ) {
        try {
            Page<GroupResponseDTO> userGroups = groupService.getUserGroupsAsDTO(pageable);
            return ResponseEntity.ok(new JsonResponse(true, "User groups fetched successfully", userGroups));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to create a new group.
     * Requires the group details in the request body.
     *
     * @param groupRequestDTO The group details provided in the request body.
     * @return A ResponseEntity containing the created group details or an error message if the group cannot be created.
     */
    @Operation(summary = "Create a new group")
    @PostMapping("/create")
    public ResponseEntity<JsonResponse> createGroup(GroupRequestDTO groupRequestDTO) {
        try {
            GroupEntity createdGroup = groupService.createGroup(groupRequestDTO);
            GroupResponseDTO createdGroupResponseDTO = new GroupResponseDTO(createdGroup);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new JsonResponse(true, "Group created successfully", createdGroupResponseDTO));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to update an existing group.
     * The user must be the owner of the group to update it.
     *
     * @param groupId The ID of the group to update.
     * @param groupRequestDTO The updated group details.
     * @return A ResponseEntity containing the updated group details or an error message if the group cannot be updated.
     */
    @Operation(summary = "Update group")
    @PutMapping("/update/{groupId}")
    @PreAuthorize("@groupService.isGroupOwner(#groupId)")
    public ResponseEntity<JsonResponse> updateGroup(
            @PathVariable Long groupId,
            GroupRequestDTO groupRequestDTO
    ) {
        try {
            GroupEntity updatedGroup = groupService.updateGroup(groupId, groupRequestDTO);
            GroupResponseDTO updatedGroupResponseDTO = new GroupResponseDTO(updatedGroup);

            return ResponseEntity.ok(new JsonResponse(true, "Group updated successfully", updatedGroupResponseDTO));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to join an existing group.
     * The user must be authenticated and the group must exist.
     *
     * @param groupId The ID of the group to join.
     * @return A ResponseEntity containing a success message and the updated group or an error message if the group cannot be joined.
     */
    @Operation(summary = "Join an existing group")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<JsonResponse> joinGroup(@PathVariable Long groupId) {
        try {
            GroupEntity joinedGroup = groupService.joinGroup(groupId);
            GroupResponseDTO joinedGroupResponseDTO = new GroupResponseDTO(joinedGroup);

            return ResponseEntity.ok(new JsonResponse(true, "Joined group successfully", joinedGroupResponseDTO));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to leave an existing group.
     * The user must be part of the group to leave it.
     *
     * @param groupId The ID of the group to leave.
     * @return A ResponseEntity containing a success message and the updated group or an error message if the group cannot be left.
     */
    @Operation(summary = "Leave a group")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<JsonResponse> leaveGroup(@PathVariable Long groupId) {
        try {
            GroupEntity leavedGroup = groupService.leaveGroup(groupId);
            GroupResponseDTO leavedGroupResponseDTO = new GroupResponseDTO(leavedGroup);

            return ResponseEntity.ok(new JsonResponse(true, "Left group successfully", leavedGroupResponseDTO));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }

    /**
     * Endpoint to delete a group.
     * Only the owner of the group can delete it.
     *
     * @param groupId The ID of the group to delete.
     * @return A ResponseEntity containing a success message or an error message if the group cannot be deleted.
     */
    @Operation(summary = "Delete a group (only for the owner)")
    @PreAuthorize("@groupService.isGroupOwner(#groupId)")
    @DeleteMapping("/{groupId}/delete")
    public ResponseEntity<JsonResponse> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok(new JsonResponse(true, "Group deleted successfully", null));
        } catch (GroupNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JsonResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, e.getMessage()));
        }
    }
}
