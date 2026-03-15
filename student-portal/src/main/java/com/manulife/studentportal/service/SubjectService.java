package com.manulife.studentportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.request.CreateSubjectRequest;
import com.manulife.studentportal.dto.request.UpdateSubjectRequest;
import com.manulife.studentportal.dto.response.SubjectResponse;

public interface SubjectService {

    SubjectResponse create(CreateSubjectRequest request);

    SubjectResponse getById(Long id);

    Page<SubjectResponse> getAll(Pageable pageable);

    SubjectResponse update(Long id, UpdateSubjectRequest request);

    void delete(Long id);
}
