package com.manulife.studentportal.controller;

import static com.manulife.studentportal.security.SecurityExpressions.ADMIN_OR_OWNER_TEACHER;

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

import com.manulife.studentportal.dto.request.AssignClassesRequest;
import com.manulife.studentportal.dto.request.AssignSubjectsRequest;
import com.manulife.studentportal.dto.request.CreateTeacherRequest;
import com.manulife.studentportal.dto.request.UpdateTeacherRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.dto.response.TeacherResponse;
import com.manulife.studentportal.security.AdminOnly;
import com.manulife.studentportal.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Tag(name = "Teacher Management", description = "APIs for managing teacher profiles and assignments")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @AdminOnly
    @Operation(summary = "Create teacher profile", description = "Creates a new teacher profile and links it to an existing user with TEACHER role")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request) {

        TeacherResponse response = teacherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teacher created successfully", response));
    }

    @GetMapping
    @AdminOnly
    @Operation(summary = "Get all teachers", description = "Retrieves a paginated list of all teachers")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAllTeachers(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
                

        Page<TeacherResponse> teachers = teacherService.getAll(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(teachers.getNumber())
                .size(teachers.getSize())
                .totalElements(teachers.getTotalElements())
                .totalPages(teachers.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", teachers.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Get teacher by ID", description = "Retrieves a specific teacher's details including assigned classes and subjects")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        TeacherResponse response = teacherService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Update teacher profile", description = "Updates teacher's name and phone. Staff ID is not updatable.")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request) {

        TeacherResponse response = teacherService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Teacher updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Delete teacher", description = "Soft deletes a teacher profile")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        teacherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher deleted successfully", null));
    }

    @PutMapping("/{id}/classes")
    @AdminOnly
    @Operation(summary = "Assign classes to teacher", description = "Replaces the entire list of assigned classes for a teacher (idempotent PUT)")
    public ResponseEntity<ApiResponse<Void>> assignClasses(
            @Parameter(description = "Teacher ID") @PathVariable Long id,
            @Valid @RequestBody AssignClassesRequest request) {

        teacherService.assignClasses(id, request);
        return ResponseEntity.ok(ApiResponse.success("Classes assigned successfully", null));
    }

    @PutMapping("/{id}/subjects")
    @AdminOnly
    @Operation(summary = "Assign subjects to teacher", description = "Replaces the entire list of assigned subjects for a teacher (idempotent PUT)")
    public ResponseEntity<ApiResponse<Void>> assignSubjects(
            @Parameter(description = "Teacher ID") @PathVariable Long id,
            @Valid @RequestBody AssignSubjectsRequest request) {

        teacherService.assignSubjects(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subjects assigned successfully", null));
    }

    @GetMapping("/{id}/classes")
    @PreAuthorize(ADMIN_OR_OWNER_TEACHER)
    @Operation(summary = "Get assigned classes", description = "Retrieves the list of classes assigned to a teacher. ADMIN can view any, TEACHER can view only their own.")
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getTeacherClasses(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        List<SchoolClassResponse> classes = teacherService.getClasses(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher classes retrieved successfully", classes));
    }

    @GetMapping("/{id}/subjects")
    @PreAuthorize(ADMIN_OR_OWNER_TEACHER)
    @Operation(summary = "Get assigned subjects", description = "Retrieves the list of subjects assigned to a teacher. ADMIN can view any, TEACHER can view only their own.")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getTeacherSubjects(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        List<SubjectResponse> subjects = teacherService.getSubjects(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher subjects retrieved successfully", subjects));
    }
}
