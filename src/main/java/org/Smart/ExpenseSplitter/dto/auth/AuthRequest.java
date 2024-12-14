package org.Smart.ExpenseSplitter.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String email;
    private String password;
}

