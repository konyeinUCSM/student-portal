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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.dto.request.BatchMarkRequest;
import com.manulife.studentportal.dto.request.CreateMarkRequest;
import com.manulife.studentportal.dto.request.UpdateMarkRequest;
import com.manulife.studentportal.dto.response.ApiResponse;
import com.manulife.studentportal.dto.response.MarkResponse;
import com.manulife.studentportal.dto.response.PaginationMeta;
import com.manulife.studentportal.security.AdminOnly;
import com.manulife.studentportal.security.AdminOrTeacher;
import com.manulife.studentportal.security.AdminTeacherOrStudent;
import com.manulife.studentportal.security.TeacherOnly;
import com.manulife.studentportal.service.MarkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/marks")
@RequiredArgsConstructor
@Tag(name = "Mark Management", description = "APIs for managing student marks")
public class MarkController {

    private final MarkService markService;

    @PostMapping
    @TeacherOnly
    @Operation(summary = "Create single mark", description = "Enters a mark for a student in an exam. TEACHER must be assigned to both the exam's class and subject.")
    public ResponseEntity<ApiResponse<MarkResponse>> createMark(
            @Valid @RequestBody CreateMarkRequest request) {

        MarkResponse response = markService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mark created successfully", response));
    }

    @PostMapping("/batch")
    @TeacherOnly
    @Operation(summary = "Create batch marks", description = "Enters marks for multiple students in an exam. TEACHER must be assigned to both the exam's class and subject.")
    public ResponseEntity<ApiResponse<Void>> createBatchMarks(
            @Valid @RequestBody BatchMarkRequest request) {

        markService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch marks created successfully", null));
    }

    @GetMapping
    @AdminOrTeacher
    @Operation(summary = "Get all marks", description = "Retrieves a paginated list of marks. ADMIN gets all, TEACHER gets marks for assigned classes/subjects. Optional filters: examId, studentId.")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getAllMarks(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable,
            @Parameter(description = "Filter by exam ID")
            @RequestParam(required = false) Long examId,
            @Parameter(description = "Filter by student ID")
            @RequestParam(required = false) Long studentId) {

        Page<MarkResponse> marks = markService.getAll(pageable, examId, studentId);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(marks.getNumber())
                .size(marks.getSize())
                .totalElements(marks.getTotalElements())
                .totalPages(marks.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", marks.getContent(), pagination));
    }

    @GetMapping("/{id}")
    @AdminTeacherOrStudent
    @Operation(summary = "Get mark by ID", description = "Retrieves a specific mark. ADMIN can view any, TEACHER can view marks for assigned classes/subjects, STUDENT can view only their own marks.")
    public ResponseEntity<ApiResponse<MarkResponse>> getMarkById(
            @Parameter(description = "Mark ID") @PathVariable Long id) {

        MarkResponse response = markService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Mark retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @TeacherOnly
    @Operation(summary = "Update mark", description = "Updates a mark's score and remarks. Grade is automatically recalculated. TEACHER must be assigned to the exam's class and subject.")
    public ResponseEntity<ApiResponse<MarkResponse>> updateMark(
            @Parameter(description = "Mark ID") @PathVariable Long id,
            @Valid @RequestBody UpdateMarkRequest request) {

        MarkResponse response = markService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Mark updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "Delete mark", description = "Soft deletes a mark. ADMIN only.")
    public ResponseEntity<ApiResponse<Void>> deleteMark(
            @Parameter(description = "Mark ID") @PathVariable Long id) {

        markService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Mark deleted successfully", null));
    }
}
