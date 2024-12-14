package org.Smart.ExpenseSplitter.service;

import org.Smart.ExpenseSplitter.dto.auth.AuthRequest;
import org.Smart.ExpenseSplitter.dto.auth.AuthResponse;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.Smart.ExpenseSplitter.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(authRequest.getEmail());

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(token, "Login successful");
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(authRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        newUser.setUsername(authRequest.getEmail().split("@")[0]); // Default username

        userRepository.save(newUser);
        String token = jwtUtil.generateToken(newUser.getEmail());
        return new AuthResponse(token, "User registered and logged in");
    }
}
