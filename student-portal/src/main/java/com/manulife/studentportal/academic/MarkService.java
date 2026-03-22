package com.manulife.studentportal.academic;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.academic.web.BatchMarkRequest;
import com.manulife.studentportal.academic.web.CreateMarkRequest;
import com.manulife.studentportal.academic.web.UpdateMarkRequest;
import com.manulife.studentportal.academic.web.GradeSummaryResponse;
import com.manulife.studentportal.academic.web.MarkResponse;

public interface MarkService {

    MarkResponse create(CreateMarkRequest request);

    void createBatch(BatchMarkRequest request);

    MarkResponse getById(Long id);

    Page<MarkResponse> getAll(Pageable pageable, Long examId, Long studentId);

    MarkResponse update(Long id, UpdateMarkRequest request);

    void delete(Long id);

    /**
     * Get marks for a student, paginated
     */
    Page<MarkResponse> getMarksByStudent(Long studentId, Pageable pageable);

    /**
     * Get marks for a student and specific exam
     */
    List<MarkResponse> getMarksByStudentAndExam(Long studentId, Long examId);

    /**
     * Calculate grade summary for a student
     */
    GradeSummaryResponse getGradeSummary(Long studentId);
}
