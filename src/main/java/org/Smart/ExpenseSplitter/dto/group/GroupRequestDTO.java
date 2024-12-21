package org.Smart.ExpenseSplitter.dto.group;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating or updating a group.
 */
@Data
public class GroupRequestDTO {

    @NotNull
    private String name;
}

