package com.manulife.studentportal.academic.web;

import static com.manulife.studentportal.auth.SecurityExpressions.ADMIN_OR_ASSIGNED_TO_SUBJECT;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.shared.dto.ApiResponse;
import com.manulife.studentportal.shared.dto.PaginationMeta;
import com.manulife.studentportal.shared.annotation.AdminOnly;
import com.manulife.studentportal.shared.annotation.AdminOrTeacher;
import com.manulife.studentportal.academic.SubjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Tag(name = "Subject Management", description = "APIs for managing subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @AdminOnly
    @Operation(summary = "Create subject", description = "Creates a new subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {

        SubjectResponse response = subjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created successfully", response));
    }

    @GetMapping
    @AdminOrTeacher
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
    @PreAuthorize(ADMIN_OR_ASSIGNED_TO_SUBJECT)
    @Operation(summary = "Get subject by ID", description = "Retrieves a specific subject's details. ADMIN can view any, TEACHER can view only assigned subjects.")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable Long id) {

        SubjectResponse response = subjectService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Update subject", description = "Updates a subject's name")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        SubjectResponse response = subjectService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Delete subject", description = "Soft deletes a subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @Parameter(description = "Subject ID") @PathVariable Long id) {

        subjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted successfully", null));
    }
}
