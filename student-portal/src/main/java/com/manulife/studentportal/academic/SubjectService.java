package com.manulife.studentportal.academic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.academic.web.CreateSubjectRequest;
import com.manulife.studentportal.academic.web.UpdateSubjectRequest;
import com.manulife.studentportal.academic.web.SubjectResponse;

public interface SubjectService {

    SubjectResponse create(CreateSubjectRequest request);

    SubjectResponse getById(Long id);

    SubjectInfo getSubjectInfoById(Long id);

    Page<SubjectResponse> getAll(Pageable pageable);

    SubjectResponse update(Long id, UpdateSubjectRequest request);

    void delete(Long id);
}
