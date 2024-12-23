package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.dto.group.GroupRequestDTO;
import org.Smart.ExpenseSplitter.dto.group.GroupResponseDTO;
import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final AuthService userService;

    public GroupService(GroupRepository groupRepository, AuthService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }


    public GroupEntity getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with ID " + groupId + " not found"));
    }

    @Transactional
    public boolean isCurrentUserGroupOwner(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        UserEntity currentUser = userService.getCurrentUser();
        return group.getOwner().getId().equals(currentUser.getId());
    }


    @Transactional
    public boolean isCurrentUserMemberOfGroup(Long groupId) {
        UserEntity currentUser = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        return group.getMembers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    @Transactional
    public boolean isCurrentUserMemberOrOwnerOfGroup(Long groupId) {
        UserEntity currentUser = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        return group.getOwner().getId().equals(currentUser.getId()) ||
                group.getMembers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
    }

    @Transactional
    public boolean isUserMemberOrOwnerOfGroup(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        return group.getMembers().stream()
                .anyMatch(user -> user.getId().equals(userId)) || group.getOwner().getId().equals(userId);
    }

    public GroupEntity getGroupDetail(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
    }

    public Page<GroupEntity> getUserGroups(Pageable pageable) {
        UserEntity currentUser = userService.getCurrentUser();
        return groupRepository.findByOwner(currentUser, pageable);
    }

    public GroupEntity createGroup(GroupRequestDTO groupRequestDTO) {
        UserEntity creator = userService.getCurrentUser();
        GroupEntity group = new GroupEntity();
        group.setName(groupRequestDTO.getName());
        group.setOwner(creator);

        return groupRepository.save(group);
    }

    public GroupEntity updateGroup(Long groupId, GroupRequestDTO groupRequestDTO) {
        GroupEntity group = getGroupDetail(groupId);
        group.setName(groupRequestDTO.getName());

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

        if (group.getOwner().equals(currentUser)) {
            throw new BadRequestException("Can not join group because you are the group owner");
        }

        if (group.getMembers().contains(currentUser)) {
            throw new BadRequestException("User is already a member of the group");
        }

        group.getMembers().add(currentUser);
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

        if (group.getOwner().equals(currentUser)) {
            throw new BadRequestException("You are the group owner, delete the group instead");
        }

        if (!group.getMembers().contains(currentUser)) {
            throw new BadRequestException("User is not a member of the group");
        }

        group.getMembers().remove(currentUser);
        return groupRepository.save(group);
    }

    /**
     * Deletes a group by its ID.
     *
     * @param groupId The ID of the group to delete.
     * @throws GroupNotFoundException if the group does not exist.
     * @throws BadRequestException    if the group cannot be deleted due to an internal error.
     */
    @Transactional
    public void deleteGroup(Long groupId) throws BadRequestException {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        groupRepository.delete(group);
    }

    /**
     * Retrieves a paginated list of groups the current authenticated user belongs to, in the form of DTOs.
     *
     * @param pageable Pagination information for retrieving groups.
     * @return A page of GroupResponseDTOs the current user is part of.
     */
    public Page<GroupResponseDTO> getUserGroupsAsDTO(Pageable pageable) {
        Page<GroupEntity> userGroups = getUserGroups(pageable);

        List<GroupResponseDTO> groupResponseDTOs = userGroups.getContent().stream()
                .map(GroupResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(groupResponseDTOs, userGroups.getPageable(), userGroups.getTotalElements());
    }
}
