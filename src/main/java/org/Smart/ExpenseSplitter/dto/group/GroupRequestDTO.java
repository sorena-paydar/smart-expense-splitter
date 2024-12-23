package org.Smart.ExpenseSplitter.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating or updating a group.
 */
@Data
public class GroupRequestDTO {

    @Schema(description = "Group's name", example = "Safar shomal")
    @NotNull
    private String name;
}

