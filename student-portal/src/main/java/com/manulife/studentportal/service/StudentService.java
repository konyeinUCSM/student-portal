package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.request.CreateStudentRequest;
import com.manulife.studentportal.dto.request.UpdateStudentRequest;
import com.manulife.studentportal.dto.response.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponse create(CreateStudentRequest request);

    StudentResponse getById(Long id);

    Page<StudentResponse> getAll(Pageable pageable);

    StudentResponse update(Long id, UpdateStudentRequest request);

    void delete(Long id);
}
