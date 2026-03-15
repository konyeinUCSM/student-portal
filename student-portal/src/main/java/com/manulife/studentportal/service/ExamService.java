package com.manulife.studentportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.request.CreateExamRequest;
import com.manulife.studentportal.dto.request.UpdateExamRequest;
import com.manulife.studentportal.dto.response.ExamResponse;

public interface ExamService {

    ExamResponse create(CreateExamRequest request);

    ExamResponse getById(Long id);

    Page<ExamResponse> getAll(Pageable pageable);

    ExamResponse update(Long id, UpdateExamRequest request);

    void delete(Long id);
}
