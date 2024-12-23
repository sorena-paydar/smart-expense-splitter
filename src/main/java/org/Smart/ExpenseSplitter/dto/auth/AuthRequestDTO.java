package org.Smart.ExpenseSplitter.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequestDTO {

    @Schema(description = "Username for login", example = "test")
    private String username;

    @Schema(description = "Email address for login", example = "test@gmail.com")
    private String email;

    @Schema(description = "User's password", example = "1234")
    private String password;
}

