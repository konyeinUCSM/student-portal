package com.manulife.studentportal.controller;

import com.manulife.studentportal.dto.request.CreateSubjectRequest;
import com.manulife.studentportal.dto.request.UpdateSubjectRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.service.SubjectService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Tag(name = "Subject Management", description = "APIs for managing subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create subject", description = "Creates a new subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {

        SubjectResponse response = subjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all subjects", description = "Retrieves a paginated list of subjects. ADMIN gets all, TEACHER gets only assigned subjects.")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<SubjectResponse> subjects = subjectService.getAll(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(subjects.getNumber())
                .size(subjects.getSize())
                .totalElements(subjects.getTotalElements())
                .totalPages(subjects.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved successfully", subjects.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.isTeacherAssignedToSubject(#id))")
    @Operation(summary = "Get subject by ID", description = "Retrieves a specific subject's details. ADMIN can view any, TEACHER can view only assigned subjects.")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable Long id) {

        SubjectResponse response = subjectService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update subject", description = "Updates a subject's name")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        SubjectResponse response = subjectService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete subject", description = "Soft deletes a subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id) {

        subjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted successfully", null));
    }
}
