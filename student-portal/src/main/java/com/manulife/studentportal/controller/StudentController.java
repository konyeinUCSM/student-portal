package com.manulife.studentportal.controller;

import com.manulife.studentportal.dto.request.CreateStudentRequest;
import com.manulife.studentportal.dto.request.UpdateStudentRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.service.StudentService;
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
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs for managing student profiles")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create student profile", description = "Creates a new student profile and links it to an existing user with STUDENT role")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {

        StudentResponse response = studentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all students", description = "Retrieves a paginated list of students. ADMIN gets all, TEACHER gets only students in assigned classes.")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<StudentResponse> students = studentService.getAll(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(students.getNumber())
                .size(students.getSize())
                .totalElements(students.getTotalElements())
                .totalPages(students.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and @securityService.isStudentOwner(#id))")
    @Operation(summary = "Get student by ID", description = "Retrieves a specific student's details. ADMIN can view any, TEACHER can view students in assigned classes, STUDENT can view only their own profile.")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @Parameter(description = "Student ID") @PathVariable Long id) {

        StudentResponse response = studentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update student profile", description = "Updates student's name, phone, date of birth, and class. Roll number is not updatable.")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Parameter(description = "Student ID") @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {

        StudentResponse response = studentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete student", description = "Soft deletes a student profile")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {

        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }
}
