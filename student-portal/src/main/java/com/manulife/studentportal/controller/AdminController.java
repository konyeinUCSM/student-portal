package com.manulife.studentportal.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.manulife.studentportal.security.AdminOnly;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.service.LoginSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@AdminOnly
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin-only APIs for system monitoring and session management")
public class AdminController {

    private final LoginSessionService loginSessionService;

    @GetMapping("/sessions")
    @Operation(summary = "Get all sessions", description = "Retrieves a paginated list of all login sessions with optional filters for userId and active status")
    public ResponseEntity<ApiResponse<List<LoginSessionResponse>>> getAllSessions(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {

        Page<LoginSessionResponse> sessions = loginSessionService.getAllSessions(pageable, userId, active);

        return ResponseEntity.ok(ApiResponse.success(
                "Sessions retrieved successfully",
                sessions.getContent(),
                buildPaginationMeta(sessions)));
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Terminate session", description = "Force-terminates a login session by setting it as inactive. The user's next request with that token will receive 401.")
    public ResponseEntity<ApiResponse<Void>> terminateSession(
            @Parameter(description = "Session ID") @PathVariable Long id) {

        loginSessionService.terminateSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session terminated successfully", null));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves aggregate counts for the admin dashboard: users, teachers, students, classes, subjects, exams, and active sessions")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {

        DashboardStatsResponse stats = loginSessionService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    private PaginationMeta buildPaginationMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}