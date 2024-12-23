package org.Smart.ExpenseSplitter.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.Smart.ExpenseSplitter.entity.UserEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDTO(
        Long id,
        String username,
        String email
) {
    public UserResponseDTO(UserEntity userEntity) {
        this(userEntity.getId(), userEntity.getUsername(), userEntity.getEmail());
    }
}
