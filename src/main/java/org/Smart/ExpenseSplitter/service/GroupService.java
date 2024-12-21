package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.exception.UserNotFoundException;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final AuthService userService;

    public GroupService(GroupRepository groupRepository, AuthService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    /**
     * Checks if the current authenticated user is the owner of the given group.
     *
     * @param groupId The ID of the group to check ownership.
     * @return true if the user is the owner of the group, false otherwise.
     */
    @Transactional
    public boolean isGroupOwner(Long groupId) {
        Optional<GroupEntity> groupEntityOptional = groupRepository.findById(groupId);
        if (groupEntityOptional.isEmpty()) {
            return false;
        }

        GroupEntity group = groupEntityOptional.get();

        UserEntity currentUser = userService.getCurrentUser();
        return group.getCreator().getId().equals(currentUser.getId());
    }

    /**
     * Get a group by its ID.
     *
     * @param groupId ID of the group to fetch.
     * @return GroupEntity if found.
     * @throws GroupNotFoundException if the group with the given ID does not exist.
     */
    public GroupEntity getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with ID " + groupId + " not found"));
    }

    /**
     * Checks if the current authenticated user is a member of the given group.
     *
     * @param groupId The ID of the group to check membership.
     * @return true if the user is a member of the group, false otherwise.
     */
    @Transactional
    public boolean isUserMemberOfGroup(Long groupId) {
        UserEntity currentUser = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        return group.getUsers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    @Transactional
    public boolean isUserIdMemberOrOwnerOfGroup(GroupEntity group, Long userId) {
        return group.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    /**
     * Retrieves detailed information about a group by its ID.
     *
     * @param groupId The ID of the group to fetch.
     * @return GroupEntity containing the group's information.
     * @throws GroupNotFoundException if the group is not found.
     */
    public GroupEntity getGroupDetail(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
    }

    /**
     * Retrieves all groups the current authenticated user belongs to.
     *
     * @param pageable Pagination information for retrieving groups.
     * @return A page of GroupEntities the current user is part of.
     */
    public Page<GroupEntity> getUserGroups(Pageable pageable) {
        UserEntity currentUser = userService.getCurrentUser();
        return groupRepository.findCurrentUserGroups(currentUser, pageable);
    }

    /**
     * Creates a new group with the current authenticated user as the creator.
     *
     * @param groupName The name of the new group.
     * @return The created GroupEntity.
     */
    public GroupEntity createGroup(String groupName) {
        UserEntity creator = userService.getCurrentUser();
        GroupEntity group = new GroupEntity();
        group.setName(groupName);
        group.setCreator(creator);

        return groupRepository.save(group);
    }

    /**
     * Allows the current user to join an existing group.
     *
     * @param groupId The ID of the group to join.
     * @return The updated GroupEntity.
     * @throws BadRequestException if the user is the group owner or already a member of the group.
     */
    @Transactional
    public GroupEntity joinGroup(Long groupId) throws BadRequestException {
        UserEntity currentUser = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (group.getCreator().equals(currentUser)) {
            throw new BadRequestException("Can not join group because you are the group owner");
        }

        if (group.getUsers().contains(currentUser)) {
            throw new BadRequestException("User is already a member of the group");
        }

        group.getUsers().add(currentUser);
        return groupRepository.save(group);
    }

    /**
     * Allows the current user to leave an existing group.
     *
     * @param groupId The ID of the group to leave.
     * @return The updated GroupEntity.
     * @throws BadRequestException if the user is the group owner or not a member of the group.
     */
    @Transactional
    public GroupEntity leaveGroup(Long groupId) throws BadRequestException {
        UserEntity currentUser = userService.getCurrentUser();
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (group.getCreator().equals(currentUser)) {
            throw new BadRequestException("You are the group owner, delete the group instead");
        }

        if (!group.getUsers().contains(currentUser)) {
            throw new BadRequestException("User is not a member of the group");
        }

        group.getUsers().remove(currentUser);
        return groupRepository.save(group);
    }

    /**
     * Deletes a group by its ID.
     *
     * @param groupId The ID of the group to delete.
     * @throws GroupNotFoundException if the group does not exist.
     * @throws BadRequestException if the group cannot be deleted due to an internal error.
     */
    @Transactional
    public void deleteGroup(Long groupId) throws BadRequestException {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        groupRepository.delete(group);
    }
}
