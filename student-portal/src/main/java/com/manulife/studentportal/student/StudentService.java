package com.manulife.studentportal.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.student.web.CreateStudentRequest;
import com.manulife.studentportal.student.web.UpdateStudentRequest;
import com.manulife.studentportal.student.web.StudentResponse;

public interface StudentService {

    StudentResponse create(CreateStudentRequest request);

    StudentResponse getById(Long id);

    Page<StudentResponse> getAll(Pageable pageable);

    StudentResponse update(Long id, UpdateStudentRequest request);

    void delete(Long id);
}
