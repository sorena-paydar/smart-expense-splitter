package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.entity.GroupEntity;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.exception.GroupNotFoundException;
import org.Smart.ExpenseSplitter.repository.GroupRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Transactional
    public boolean isGroupOwner(UserDetails principal, Long groupId) {
        Optional<GroupEntity> groupEntityOptional = groupRepository.findById(groupId);
        if (groupEntityOptional.isEmpty()) {
            return false;
        }

        GroupEntity group = groupEntityOptional.get();

        UserEntity currentUser = userService.getCurrentUser();
        return group.getCreator().equals(currentUser);
    }

    public GroupEntity getGroupDetail(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
    }

    public Page<GroupEntity> getUserGroups(Pageable pageable) {
        UserEntity user = userService.getCurrentUser();
        return groupRepository.findCurrentUserGroups(user, pageable);
    }

    public GroupEntity createGroup(String groupName) {
        UserEntity creator = userService.getCurrentUser();
        GroupEntity group = new GroupEntity();
        group.setName(groupName);
        group.setCreator(creator);

        return groupRepository.save(group);
    }

    @Transactional
    public GroupEntity joinGroup(Long groupId) throws BadRequestException {
        UserEntity user = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (group.getCreator().equals(user)) {
            throw new BadRequestException("Can not join group because you are the group owner");
        }

        if (group.getUsers().contains(user)) {
            throw new BadRequestException("User is already a member of the group");
        }

        group.getUsers().add(user);
        return groupRepository.save(group);
    }

    @Transactional
    public GroupEntity leaveGroup(Long groupId) throws BadRequestException {
        UserEntity user = userService.getCurrentUser();
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (group.getCreator().equals(user)) {
            throw new BadRequestException("You are the group owner, delete the group instead");
        }

        if (!group.getUsers().contains(user)) {
            throw new BadRequestException("User is not a member of the group");
        }

        group.getUsers().remove(user);
        return groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long groupId) throws BadRequestException {
        UserEntity user = userService.getCurrentUser();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        groupRepository.delete(group);
    }
}
