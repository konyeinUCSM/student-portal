package com.manulife.studentportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.request.BatchMarkRequest;
import com.manulife.studentportal.dto.request.CreateMarkRequest;
import com.manulife.studentportal.dto.request.UpdateMarkRequest;
import com.manulife.studentportal.dto.response.MarkResponse;

public interface MarkService {

    MarkResponse create(CreateMarkRequest request);

    void createBatch(BatchMarkRequest request);

    MarkResponse getById(Long id);

    Page<MarkResponse> getAll(Pageable pageable, Long examId, Long studentId);

    MarkResponse update(Long id, UpdateMarkRequest request);

    void delete(Long id);
}
