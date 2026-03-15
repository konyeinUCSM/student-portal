package com.manulife.studentportal.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.dto.request.CreateExamRequest;
import com.manulife.studentportal.dto.request.UpdateExamRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.ExamResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.security.AdminOnly;
import com.manulife.studentportal.security.AdminOrTeacher;
import com.manulife.studentportal.security.AdminTeacherOrStudent;
import com.manulife.studentportal.service.ExamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exam Management", description = "APIs for managing exams")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @AdminOrTeacher
    @Operation(summary = "Create exam", description = "Creates a new exam. ADMIN can create for any class/subject. TEACHER can only create for assigned class AND subject.")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @Valid @RequestBody CreateExamRequest request) {

        ExamResponse response = examService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exam created successfully", response));
    }

    @GetMapping
    @AdminTeacherOrStudent
    @Operation(summary = "Get all exams", description = "Retrieves a paginated list of exams. ADMIN gets all, TEACHER gets exams for assigned classes, STUDENT gets exams for their class.")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<ExamResponse> exams = examService.getAll(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(exams.getNumber())
                .size(exams.getSize())
                .totalElements(exams.getTotalElements())
                .totalPages(exams.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Exams retrieved successfully", exams.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @AdminTeacherOrStudent
    @Operation(summary = "Get exam by ID", description = "Retrieves a specific exam's details. ADMIN can view any, TEACHER can view exams for assigned classes, STUDENT can view exams for their class.")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(
            @Parameter(description = "Exam ID") @PathVariable Long id) {

        ExamResponse response = examService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Exam retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @AdminOrTeacher
    @Operation(summary = "Update exam", description = "Updates exam details (name, date, marks). Class and subject are NOT updatable. ADMIN can update any, TEACHER can only update exams they created.")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @Parameter(description = "Exam ID") @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest request) {

        ExamResponse response = examService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Exam updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Delete exam", description = "Soft deletes an exam. ADMIN only.")
    public ResponseEntity<ApiResponse<Void>> deleteExam(
            @Parameter(description = "Exam ID") @PathVariable Long id) {

        examService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted successfully", null));
    }
}
