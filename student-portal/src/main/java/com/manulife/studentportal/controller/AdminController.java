package com.manulife.studentportal.controller;

import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.service.LoginSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin-only APIs for system monitoring and session management")
public class AdminController {

    private final LoginSessionService loginSessionService;

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all sessions", description = "Retrieves a paginated list of all login sessions with optional filters for userId and active status")
    public ResponseEntity<ApiResponse<List<LoginSessionResponse>>> getAllSessions(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {

        Page<LoginSessionResponse> sessions = loginSessionService.getAllSessions(pageable, userId, active);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(sessions.getNumber())
                .size(sessions.getSize())
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved successfully", sessions.getContent(), pagination));
    }

    @GetMapping("/sessions/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active sessions", description = "Retrieves a paginated list of currently active login sessions")
    public ResponseEntity<ApiResponse<List<LoginSessionResponse>>> getActiveSessions(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<LoginSessionResponse> sessions = loginSessionService.getActiveSessions(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(sessions.getNumber())
                .size(sessions.getSize())
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved successfully", sessions.getContent(), pagination));
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Terminate session", description = "Force-terminates a login session by setting it as inactive. The user's next request with that token will receive 401.")
    public ResponseEntity<ApiResponse<Void>> terminateSession(
            @Parameter(description = "Session ID") @PathVariable Long id) {

        loginSessionService.terminateSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session terminated successfully", null));
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves aggregate counts for the admin dashboard: users, teachers, students, classes, subjects, exams, and active sessions")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {

        DashboardStatsResponse stats = loginSessionService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }
}
