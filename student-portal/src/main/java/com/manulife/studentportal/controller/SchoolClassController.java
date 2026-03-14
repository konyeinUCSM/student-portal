package com.manulife.studentportal.controller;

import com.manulife.studentportal.dto.request.CreateClassRequest;
import com.manulife.studentportal.dto.request.UpdateClassRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.service.SchoolClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.manulife.studentportal.security.AdminOnly;
import com.manulife.studentportal.security.AdminOrTeacher;
import static com.manulife.studentportal.security.SecurityExpressions.ADMIN_OR_ASSIGNED_TO_CLASS;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Class Management", description = "APIs for managing school classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @PostMapping
    @AdminOnly
    @Operation(summary = "Create class", description = "Creates a new school class")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> createClass(
            @Valid @RequestBody CreateClassRequest request) {

        SchoolClassResponse response = schoolClassService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Class created successfully", response));
    }

    @GetMapping
    @AdminOrTeacher
    @Operation(summary = "Get all classes", description = "Retrieves a paginated list of classes. ADMIN gets all, TEACHER gets only assigned classes.")
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getAllClasses(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<SchoolClassResponse> classes = schoolClassService.getAll(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(classes.getNumber())
                .size(classes.getSize())
                .totalElements(classes.getTotalElements())
                .totalPages(classes.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Classes retrieved successfully", classes.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_OR_ASSIGNED_TO_CLASS)
    @Operation(summary = "Get class by ID", description = "Retrieves a specific class's details. ADMIN can view any, TEACHER can view only assigned classes.")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> getClassById(
            @Parameter(description = "Class ID") @PathVariable Long id) {

        SchoolClassResponse response = schoolClassService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Class retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Update class", description = "Updates a class's name")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> updateClass(
            @Parameter(description = "Class ID") @PathVariable Long id,
            @Valid @RequestBody UpdateClassRequest request) {

        SchoolClassResponse response = schoolClassService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Class updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Delete class", description = "Soft deletes a school class")
    public ResponseEntity<ApiResponse<Void>> deleteClass(
            @Parameter(description = "Class ID") @PathVariable Long id) {

        schoolClassService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Class deleted successfully", null));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize(ADMIN_OR_ASSIGNED_TO_CLASS)
    @Operation(summary = "Get students in class", description = "Retrieves the list of students enrolled in a specific class. ADMIN can view any, TEACHER can view only assigned classes.")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getClassStudents(
            @Parameter(description = "Class ID") @PathVariable Long id,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        List<StudentResponse> students = schoolClassService.getStudents(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
    }
}
