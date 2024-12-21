package org.Smart.ExpenseSplitter.dto.user;

import org.Smart.ExpenseSplitter.entity.UserEntity;

public record UserResponseDTO(
        Long id,
        String username,
        String email
) {
    public UserResponseDTO(String username, String email) {
        this(null, username, email);
    }

    public UserResponseDTO(UserEntity userEntity) {
        this(null, userEntity.getUsername(), userEntity.getEmail());
    }
}
