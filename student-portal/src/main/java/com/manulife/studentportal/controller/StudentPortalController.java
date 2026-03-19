package com.manulife.studentportal.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manulife.studentportal.shared.dto.ApiResponse;
import com.manulife.studentportal.dto.response.GradeSummaryResponse;
import com.manulife.studentportal.dto.response.MarkResponse;
import com.manulife.studentportal.shared.dto.PaginationMeta;
import com.manulife.studentportal.security.StudentOnly;
import com.manulife.studentportal.service.StudentPortalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/student-portal")
@RequiredArgsConstructor
@Tag(name = "Student Portal", description = "Self-view APIs for students to view their own marks and grades")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    @GetMapping("/marks")
    @StudentOnly
    @Operation(summary = "Get my marks", description = "Retrieves all marks for the logged-in student. Student ID is extracted from JWT, never from URL.")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getMyMarks(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        Page<MarkResponse> marks = studentPortalService.getMyMarks(pageable);
        PaginationMeta pagination = PaginationMeta.builder()
                .page(marks.getNumber())
                .size(marks.getSize())
                .totalElements(marks.getTotalElements())
                .totalPages(marks.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", marks.getContent(), pagination));
    }

    @GetMapping("/marks/by-exam/{examId}")
    @StudentOnly
    @Operation(summary = "Get marks by exam", description = "Retrieves marks for the logged-in student for a specific exam. Validates that the exam belongs to the student's class.")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getMarksByExam(
            @Parameter(description = "Exam ID") @PathVariable Long examId) {

        List<MarkResponse> marks = studentPortalService.getMarksByExam(examId);
        return ResponseEntity.ok(ApiResponse.success("Marks retrieved successfully", marks));
    }

    @GetMapping("/grades/summary")
    @StudentOnly
    @Operation(summary = "Get grade summary", description = "Computes and returns the grade summary for the logged-in student, including subject-wise averages and overall grade.")
    public ResponseEntity<ApiResponse<GradeSummaryResponse>> getGradeSummary() {

        GradeSummaryResponse summary = studentPortalService.getGradeSummary();
        return ResponseEntity.ok(ApiResponse.success("Grade summary retrieved successfully", summary));
    }
}
