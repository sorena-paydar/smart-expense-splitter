package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.dto.auth.AuthRequestDTO;
import org.Smart.ExpenseSplitter.dto.auth.JwtTokenResponseDTO;
import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.Smart.ExpenseSplitter.repository.UserRepository;
import org.Smart.ExpenseSplitter.service.AuthService;
import org.Smart.ExpenseSplitter.service.JwtService;
import org.Smart.ExpenseSplitter.util.ValidationUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Single endpoint for login and registration")
public class AuthController {

    private final JwtService tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtService tokenProvider, PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Authenticate user and generate JWT tokens")
    @PostMapping("/register")
    public ResponseEntity<JsonResponse> registerUser(
            @Valid @RequestBody AuthRequestDTO authRequestDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationUtils.generateValidationResponse(bindingResult);
        }

        try {
            // Check if the email already exists
            Optional<UserEntity> existingUserOptional = userRepository.findByEmail(authRequestDTO.getEmail());

            if (existingUserOptional.isPresent()) {
                UserEntity existingUser = existingUserOptional.get();

                // Check if the provided credentials match the existing user
                if (passwordEncoder.matches(authRequestDTO.getPassword(), existingUser.getPassword())) {
                    // Authenticate the user and generate a JWT token
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    existingUser.getUsername(), authRequestDTO.getPassword()));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    String accessToken = tokenProvider.generateToken(authentication);
                    JwtTokenResponseDTO tokenResponse = new JwtTokenResponseDTO(accessToken);

                    return ResponseEntity.ok(new JsonResponse(true, "Login successful", tokenResponse));
                } else {
                    // Password doesn't match
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new JsonResponse(false, "Invalid credentials"));
                }
            }

            // Create and save a new user if the email doesn't exist
            UserEntity newUser = new UserEntity();
            newUser.setUsername(authRequestDTO.getUsername());
            newUser.setEmail(authRequestDTO.getEmail());
            newUser.setPassword(passwordEncoder.encode(authRequestDTO.getPassword()));
            userRepository.save(newUser);

            // Authenticate and generate a JWT for the newly created user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            newUser.getUsername(), authRequestDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.generateToken(authentication);
            JwtTokenResponseDTO tokenResponse = new JwtTokenResponseDTO(accessToken);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new JsonResponse(true, "Registration successful", tokenResponse));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JsonResponse(false, "Username or email already taken"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JsonResponse(false, "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JsonResponse(false, "An error occurred: " + e.getMessage()));
        }
    }
}
