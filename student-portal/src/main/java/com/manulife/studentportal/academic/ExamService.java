package com.manulife.studentportal.academic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.academic.web.CreateExamRequest;
import com.manulife.studentportal.academic.web.UpdateExamRequest;
import com.manulife.studentportal.academic.web.ExamResponse;

public interface ExamService {

    ExamResponse create(CreateExamRequest request);

    ExamResponse getById(Long id);

    Page<ExamResponse> getAll(Pageable pageable);

    ExamResponse update(Long id, UpdateExamRequest request);

    void delete(Long id);
}
