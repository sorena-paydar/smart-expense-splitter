package org.Smart.ExpenseSplitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.Smart.ExpenseSplitter.dto.JsonResponse;
import org.Smart.ExpenseSplitter.dto.auth.AuthRequestDTO;
import org.Smart.ExpenseSplitter.dto.auth.JwtTokenResponseDTO;
import org.Smart.ExpenseSplitter.service.AuthService;
import org.Smart.ExpenseSplitter.service.JwtService;
import org.Smart.ExpenseSplitter.util.ValidationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Single endpoint for login and registration")
public class AuthController {

    private final AuthService authService;
    private final JwtService tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, JwtService tokenProvider, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Authenticate user and generate JWT tokens")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            })
    @PostMapping("/register")
    public ResponseEntity<JsonResponse> authenticateUser(
            @Valid @RequestBody AuthRequestDTO authRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationUtils.generateValidationResponse(bindingResult);
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    authRequestDTO.getUsername(), authRequestDTO.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.generateToken(authentication);
            JwtTokenResponseDTO tokenResponse = new JwtTokenResponseDTO(accessToken);

            return new ResponseEntity<>(
                    new JsonResponse(true, "Login successfully", tokenResponse), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(
                    new JsonResponse(false, "Credentials incorrect"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new JsonResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
