package com.manulife.studentportal.user.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.shared.dto.ApiResponse;
import com.manulife.studentportal.shared.dto.PaginationMeta;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.user.UserService;
import com.manulife.studentportal.shared.annotation.AdminOnly;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Management", description = "APIs for managing users (Admin only)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@AdminOnly
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the specified role (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.create(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(user)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination and optional role filter (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction") @RequestParam(defaultValue = "id,asc") String sort,
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<UserResponse> userPage = userService.getAll(pageable, role);

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message("Users retrieved successfully")
                .data(userPage.getContent())
                .pagination(PaginationMeta.builder()
                        .page(page)
                        .size(size)
                        .totalElements(userPage.getTotalElements())
                        .totalPages(userPage.getTotalPages())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getById(id);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User retrieved successfully")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user email and active status (Admin only). Username and role cannot be updated.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.update(id, request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft deletes a user by setting deleted flag and deactivating the account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("User deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}