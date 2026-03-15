package com.manulife.studentportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.dto.request.ChangePasswordRequest;
import com.manulife.studentportal.dto.request.LoginRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.LoginResponse;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final AuthService authService;
    private final SecurityService securityService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        // Extract IP address from request
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        LoginResponse response = authService.login(loginRequest, ipAddress);

        return ResponseEntity.ok(
            ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build()
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate current session")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String tokenId = securityService.getCurrentTokenId();
        authService.logout(tokenId);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build()
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<ApiResponse<LoginResponse.UserSummary>> getCurrentUser() {
        LoginResponse.UserSummary userSummary = securityService.getCurrentUserSummary();

        return ResponseEntity.ok(
            ApiResponse.<LoginResponse.UserSummary>builder()
                .success(true)
                .message("Current user retrieved successfully")
                .data(userSummary)
                .build()
        );
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for current user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        Long userId = securityService.getCurrentUserId();
        authService.changePassword(request, userId);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .build()
        );
    }
}