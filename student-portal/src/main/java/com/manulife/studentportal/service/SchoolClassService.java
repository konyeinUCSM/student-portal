package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.request.CreateClassRequest;
import com.manulife.studentportal.dto.request.UpdateClassRequest;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SchoolClassService {

    SchoolClassResponse create(CreateClassRequest request);

    SchoolClassResponse getById(Long id);

    Page<SchoolClassResponse> getAll(Pageable pageable);

    SchoolClassResponse update(Long id, UpdateClassRequest request);

    void delete(Long id);

    List<StudentResponse> getStudents(Long id, Pageable pageable);
}
